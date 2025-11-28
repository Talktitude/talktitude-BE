package edu.sookmyung.talktitude.chat.recommend.kb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
// 간단한 매칭 기반 키워드 스코어링으로 상위 k 문서 선별
public class Retriever {

    private final KnowledgeBase kb;

    // 매우 가벼운 스코어링(키워드 중복 + 카테고리 프리필터)
    public List<KnowledgeBase.Doc> retrieve(String text, String intent, int k) {
        String q = normalize(text);
        List<String> tokens = Arrays.stream(q.split("\\s+")).distinct().toList();

        return kb.all().stream()
                .filter(d -> intent == null || d.category.equalsIgnoreCase(intent))
                .sorted(Comparator.comparingDouble((KnowledgeBase.Doc d) -> -score(tokens, d)))
                .limit(k)
                .collect(Collectors.toList());
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^ㄱ-ㅎ가-힣a-z0-9\\s]", " ");
    }
    private double score(List<String> tokens, KnowledgeBase.Doc d) {
        String blob = String.join(" ", Optional.ofNullable(d.policy).orElse("") + " " +
                Optional.ofNullable(d.script).orElse("") + " " + String.join(" ", Optional.ofNullable(d.tags).orElse(List.of())) +
                " " + String.join(" ", Optional.ofNullable(d.steps).orElse(List.of())) +
                " " + String.join(" ", Optional.ofNullable(d.templates).orElse(List.of())) ).toLowerCase();
        long hit = tokens.stream().filter(blob::contains).count();
        return hit + (d.category != null ? 0.2 : 0.0);
    }
}

