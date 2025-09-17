package edu.sookmyung.talktitude.chat.service;

import edu.sookmyung.talktitude.chat.dto.ChatMessageResponse;
import edu.sookmyung.talktitude.chat.dto.MediaDto;
import edu.sookmyung.talktitude.chat.model.*;
import edu.sookmyung.talktitude.chat.repository.ChatMediaRepository;
import edu.sookmyung.talktitude.chat.repository.ChatMessageRepository;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.common.service.S3Service;
import edu.sookmyung.talktitude.common.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMediaService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMediaRepository chatMediaRepository;
    private final S3Service s3Service;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse uploadMedia(Long sessionId, SenderType senderType, List<MultipartFile> files) throws IOException {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        if (session.getStatus() == Status.FINISHED) {
            throw new BaseException(ErrorCode.INVALID_SESSION_STATE);
        }

        // 텍스트 없이 media 전용 메시지 저장
        ChatMessage message = new ChatMessage(null, session, senderType, null, null, DateTimeUtils.nowKst());
        chatMessageRepository.save(message);

        // 파일 업로드 후 DB 저장
        List<ChatMedia> medias = files.stream().map(file -> {
            try {
                String url = s3Service.upload(file);
                return chatMediaRepository.save(
                        new ChatMedia(null, message, MediaType.IMAGE, url, file.getSize(), null)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        // MediaDto 변환
        List<MediaDto> mediaDtos = medias.stream()
                .map(m -> MediaDto.builder()
                        .mediaType(m.getMediaType())
                        .url(m.getMediaUrl())
                        .size(m.getMediaSize())
                        .build())
                .toList();

        // 수신자별 페이로드(텍스트는 null, 이미지만 포함)
        ChatMessageResponse forAgent  = ChatMessageResponse.withMedias(message, "MEMBER", mediaDtos);
        ChatMessageResponse forClient = ChatMessageResponse.withMedias(message, "CLIENT", mediaDtos);

        // 동일 채널로 푸시: /user/queue/chat/{sessionId}
        messagingTemplate.convertAndSendToUser(
                session.getMember().getLoginId(),
                "/queue/chat/" + session.getId(),
                forAgent
        );
        messagingTemplate.convertAndSendToUser(
                session.getClient().getLoginId(),
                "/queue/chat/" + session.getId(),
                forClient
        );

        // 업로드 API 응답은 호출자 관점(지금은 고객 전송 가정이므로 CLIENT 뷰 반환)
        return forClient;
    }
}