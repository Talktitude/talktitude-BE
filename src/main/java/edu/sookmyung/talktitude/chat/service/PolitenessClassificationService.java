package edu.sookmyung.talktitude.chat.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolitenessClassificationService {

    @Value("${aws.bucket.onnx}")
    private String s3Bucket;

    private static final int MAX_LENGTH = 256;
    private static final long PAD_ID = 1L;
    private static final float EMOTION_THRESHOLD = 0.4f;

    // S3에서 다운로드할 파일들
    private static final String MODEL_FILE = "classifier_structure.onnx"; //ONNX로 변환한 실제 AI 모델
    private static final String TOKENIZER_FILE = "tokenizer_reduce.json"; //텍스트를 토큰으로 변환하는 규칙이 담긴 json

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;


    private AmazonS3 s3Client;
    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    // 필터링된 감정 라벨 매핑 (15개 클래스)
    private final Map<Integer, String> emotionId2Label = createEmotionLabelMap();

    private static Map<Integer, String> createEmotionLabelMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "감동/감탄");
        map.put(1, "고마움");
        map.put(2, "기쁨");
        map.put(3, "깨달음");
        map.put(4, "불평/불만");
        map.put(5, "신기함/관심");
        map.put(6, "안심/신뢰");
        map.put(7, "어이없음");
        map.put(8, "없음");
        map.put(9, "우쭐댐/무시함");
        map.put(10, "의심/불신");
        map.put(11, "짜증");
        map.put(12, "한심함");
        map.put(13, "화남/분노");
        map.put(14, "환영/호의");
        map.put(15,"증오/혐오");
        map.put(16,"불안/걱정");
        map.put(17,"안타까움/실망");
        return Collections.unmodifiableMap(map); // 수정 불가능 -> 뷰 개념
    }

    private final Map<Integer, String> politenessId2Label = Map.of(
            0, "polite",
            1, "impolite"
    );

    private static final Set<String> negativeEmotions = Set.of(
            "짜증", "화남/분노", "어이없음", "불평/불만",
            "우쭐댐/무시함", "의심/불신", "한심함","증오/혐오"
    );

    @PostConstruct
    public void init()  {
        System.out.println("S3에서 모델 다운로드 및 초기화 중...");
        try {

            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

            // S3 클라이언트 초기화
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion("ap-northeast-2")  // 명시적 리전 설정
                    .build();

            // S3에서 모델 파일들 다운로드
            Path modelPath = downloadFromS3(MODEL_FILE);
            Path tokenizerPath = downloadFromS3(TOKENIZER_FILE);

            env = OrtEnvironment.getEnvironment(); //ONNX Runtime의 전역 환경 관리자(싱글톤 패턴 -> 어플리케이션 전체에서 하나만 존재 )

            //ONNX 모델 세션 생성
            //TODO 설정 옵션들 찾아보고 필요한 것 적용하기
            try(OrtSession.SessionOptions opts = new OrtSession.SessionOptions()){ // 모델 실행 옵션 설정
                session = env.createSession(modelPath.toString(),opts);
            }

            // 토크나이저 설정
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath); //텍스트를 토큰 ID로 변환

            System.out.println("모델 초기화 완료!");
            System.out.println("S3: s3://" + s3Bucket);
            System.out.println("감정 클래스: " + emotionId2Label.size() + "개");
            System.out.println("공손함 클래스: " + politenessId2Label.size() + "개");

            session.getInputInfo().forEach((name, info) ->
                    System.out.println("입력: " + name));
            session.getOutputInfo().forEach((name, info) ->
                    System.out.println("출력: " + name));

        } catch (Exception e) {
            System.err.println("초기화 실패: " + e.getMessage());
            throw new BaseException(ErrorCode.INIT_FAILED);
        }
    }


    /**
     * S3에서 파일 다운로드 (prefix 없이 직접 접근)
     */
    private Path downloadFromS3(String fileName) throws IOException {
        System.out.println("S3 다운로드: s3://" + s3Bucket + "/" + fileName);

        try {
            GetObjectRequest request = new GetObjectRequest(s3Bucket, fileName);
            S3Object s3Object = s3Client.getObject(request);

            Path tempFile = Files.createTempFile("model_", "_" + fileName);

            try (InputStream inputStream = s3Object.getObjectContent()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            tempFile.toFile().deleteOnExit(); //JVM 종료 시 해당 임시 파일을 자동 삭제하도록 예약
            return tempFile;

        } catch (Exception e) {
            System.err.println("S3 다운로드 실패: " + fileName + e.getMessage());
            throw new BaseException(ErrorCode.S3_DOWNLOAD_FAILED);
        }
    }

    @PreDestroy
    public void cleanup() throws Exception {
        try {
            if (tokenizer != null) tokenizer.close();
            if (session != null) session.close();
            if (env != null) env.close();
            System.out.println("리소스 정리 완료");
        } catch (Exception e) {
            System.err.println("리소스 정리 중 오류: " + e.getMessage());
        }
    }

    public FilteredMultiHeadResult classify(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new FilteredMultiHeadResult(text,
                    Collections.emptyList(),
                    new PolitenessResult(-1, 0.0f, "입력 오류"),
                    "error",
                    "입력 텍스트가 비어있습니다");
        }

        try {
            //딥러닝은 고정된 입력 크기만 처리 가능하기 때문. -> 배치 처리 시 모든 샘플이 같은 크기여야 함.

            // 토크나이징
            Encoding encoding = tokenizer.encode(text);
            long[] ids = encoding.getIds(); //토큰 ID 배열 반환
            long[] mask = encoding.getAttentionMask(); // 어텐션 마스크 배열 반환(실제 토큰 1, 패딩은 0으로 표시)

            // 패딩/트렁케이션
            long[] paddedIds = new long[MAX_LENGTH];
            long[] paddedMask = new long[MAX_LENGTH];

            Arrays.fill(paddedIds, PAD_ID);
            Arrays.fill(paddedMask, 0L);

            int copyLength = Math.min(ids.length, MAX_LENGTH);
            System.arraycopy(ids, 0, paddedIds, 0, copyLength); //실제 토큰 + 패딩 -> 소스배열, 소스시작, 타겟배열, 타겟시작, 복사길이 (소스에서 타겟으로 복사)
            System.arraycopy(mask, 0, paddedMask, 0, copyLength); //실제는 1, 패딩은 0

            long[][] batchIds = new long[][]{paddedIds};
            long[][] batchMask = new long[][]{paddedMask};

            // ONNX 추론
            try (OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, batchIds);
                 OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, batchMask);
                 OrtSession.Result result = session.run(Map.of(
                         "input_ids", inputIdsTensor,
                         "attention_mask", attentionMaskTensor))) {

                // 결과 처리
                float[][] emotionProbs = (float[][]) result.get(0).getValue(); //logits[배치인덱스][클래스인덱스]
                float[][] politenessProbs = (float[][]) result.get(1).getValue();

                List<EmotionResult> emotions = processMultiLabelEmotions(emotionProbs[0]);
                PolitenessResult politeness = processPoliteness(politenessProbs[0]);
                PolitenessAnalysis analysis = analyzePoliteness(text, emotions, politeness);

                return new FilteredMultiHeadResult(text, emotions, politeness,
                        analysis.finalJudgment, analysis.reason);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new FilteredMultiHeadResult(text,
                    Collections.emptyList(),
                    new PolitenessResult(-1, 0.0f, "분류 오류"),
                    "error",
                    "분류 처리 중 오류가 발생했습니다");
        }
    }

    private List<EmotionResult> processMultiLabelEmotions(float[] probabilities) {
        List<EmotionResult> emotions = new ArrayList<>();

        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > EMOTION_THRESHOLD) {
                String label = emotionId2Label.getOrDefault(i, "unknown");
                String confidence = probabilities[i] > 0.7f ? "HIGH" :
                        probabilities[i] > 0.4f ? "MEDIUM" : "LOW";
                emotions.add(new EmotionResult(i, probabilities[i], label, confidence));
            }
        }

        emotions.sort((a, b) -> Float.compare(b.probability, a.probability));
        return emotions;
    }

    private PolitenessResult processPoliteness(float[] probabilities) {
        int predictedClass = argmax(probabilities);
        float confidence = probabilities[predictedClass];
        String label = politenessId2Label.getOrDefault(predictedClass, "unknown");

        return new PolitenessResult(predictedClass, confidence, label);
    }

    private PolitenessAnalysis analyzePoliteness(String text, List<EmotionResult> emotions,
                                                 PolitenessResult politeness) {
        float negativeScore = 0.0f;
        List<String> detectedNegative = new ArrayList<>();

        for (EmotionResult emotion : emotions) {
            if (negativeEmotions.contains(emotion.label)) {
                negativeScore += emotion.probability;
                detectedNegative.add(String.format("%s(%.2f)", emotion.label, emotion.probability));
            }
        }

        String finalJudgment;
        String reason;

        if ("impolite".equals(politeness.label)) {
            finalJudgment = "impolite";
            reason = "명시적 불공손";
        } else if (negativeScore > 1.2f) {
            finalJudgment = "impolite";
            reason = "암시적 불공손 (" + String.join(", ", detectedNegative) + ")";
        } else if (negativeScore > 0.9f) {
            finalJudgment = "borderline";
            reason = "경계선 (" + String.join(", ", detectedNegative) + ")";
        } else {
            finalJudgment = "polite";
            reason = "공손함";
        }

        return new PolitenessAnalysis(finalJudgment, reason, negativeScore, detectedNegative);
    }

    private int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    // 내부 클래스들

    private static class PolitenessAnalysis {
        public final String finalJudgment;
        public final String reason;
        public final float negativeScore;
        public final List<String> detectedNegative;

        public PolitenessAnalysis(String finalJudgment, String reason,
                                  float negativeScore, List<String> detectedNegative) {
            this.finalJudgment = finalJudgment;
            this.reason = reason;
            this.negativeScore = negativeScore;
            this.detectedNegative = detectedNegative;
        }
    }

    public static class FilteredMultiHeadResult {
        public final String text;
        public final List<EmotionResult> emotions;
        public final PolitenessResult politeness;
        public final String finalJudgment;
        public final String reason;

        public FilteredMultiHeadResult(String text, List<EmotionResult> emotions,
                                       PolitenessResult politeness, String finalJudgment, String reason) {
            this.text = text;
            this.emotions = emotions;
            this.politeness = politeness;
            this.finalJudgment = finalJudgment;
            this.reason = reason;
        }

        public EmotionResult getTopEmotion() {
            return emotions.isEmpty() ?
                    new EmotionResult(-1, 0.0f, "없음", "NONE") : emotions.get(0);
        }

        public boolean hasNegativeEmotions() {
            long negativeCount = emotions.stream()
                    .filter(e -> negativeEmotions.contains(e.label))
                    .count();
            return negativeCount >= 1;
        }

        public boolean isImpolite() {
            return "impolite".equals(finalJudgment);
        }

        @Override
        public String toString() {
            String emotionStr = emotions.stream()
                    .limit(3)
                    .map(e -> String.format("%s(%.2f)", e.label, e.probability))
                    .collect(Collectors.joining(", "));

            return String.format("FilteredMultiHeadResult{text='%s', emotions=[%s], politeness=%s, final=%s, reason='%s'}",
                    text, emotionStr, politeness.label, finalJudgment, reason);
        }
    }

    public static class EmotionResult {
        public final int predictedClass;
        public final float probability;
        public final String label;
        public final String confidence;

        public EmotionResult(int predictedClass, float probability, String label, String confidence) {
            this.predictedClass = predictedClass;
            this.probability = probability;
            this.label = label;
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return String.format("EmotionResult{class=%d, prob=%.3f, label='%s', conf=%s}",
                    predictedClass, probability, label, confidence);
        }
    }

    public static class PolitenessResult {
        public final int predictedClass;
        public final float confidence;
        public final String label;

        public PolitenessResult(int predictedClass, float confidence, String label) {
            this.predictedClass = predictedClass;
            this.confidence = confidence;
            this.label = label;
        }

        @Override
        public String toString() {
            return String.format("PolitenessResult{class=%d, confidence=%.3f, label='%s'}",
                    predictedClass, confidence, label);
        }
    }
}