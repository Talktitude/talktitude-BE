package edu.sookmyung.talktitude.chat.rag.search;

import edu.sookmyung.talktitude.chat.recommend.kb.KnowledgeBase;
import edu.sookmyung.talktitude.chat.recommend.kb.Retriever; // 기존 키워드형
import edu.sookmyung.talktitude.chat.rag.config.RagProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

/**
 * text/intent/topK를 받아 적절한 경로로 KB 문서를 가져온다.
 * RAG 토글이 꺼져 있으면(rag.enabled=false) 기존 키워드 로직 사용
 */
@Slf4j
@Component
public class DocRetrievalFacade {
    private final RagProperties props;
    private final Retriever legacyRetriever; // 기존 키워드 스코어형
    private final PgRetriever pgRetriever; // pgvector 기반


    public DocRetrievalFacade(RagProperties props, Retriever legacyRetriever, PgRetriever pgRetriever) {
        this.props = props;
        this.legacyRetriever = legacyRetriever;
        this.pgRetriever = pgRetriever;
    }

    public List<KnowledgeBase.Doc> retrieve(String text, String intent, int topK) {
        if (!props.isEnabled()) {
            // rag.enabled=false -> 레거시 키워드 검색
            return legacyRetriever.retrieve(text, intent, topK);
        }
        // rag.enabled=true -> 벡터 검색 (pgvector)
        var rows = pgRetriever.retrieve(text, intent, topK);
        List<KnowledgeBase.Doc> out = new ArrayList<>();
        for (var r : rows) {
            KnowledgeBase.Doc d = new KnowledgeBase.Doc();
            d.id = r.id();
            d.category = r.category();
            d.policy = r.snippet(); // 프롬프트 삽입용 축약 텍스트
            d.script = null; d.tags = List.of(); d.steps = List.of(); d.templates = List.of();
            out.add(d);
        }
        return out;
    }
}
