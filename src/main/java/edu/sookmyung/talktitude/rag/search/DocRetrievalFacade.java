package edu.sookmyung.talktitude.rag.search;

import edu.sookmyung.talktitude.chat.recommend.kb.KnowledgeBase;
import edu.sookmyung.talktitude.chat.recommend.kb.Retriever; // 기존 키워드형
import edu.sookmyung.talktitude.rag.config.RagProperties;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

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


    /**
     * text/intent/topK를 받아 적절한 경로로 KB 문서를 가져온다.
     * RAG 토글이 꺼져 있으면 기존 로직을 그대로 사용한다.
     */
    public List<KnowledgeBase.Doc> retrieve(String text, String intent, int topK) {
        if (!props.isEnabled()) {
            return legacyRetriever.retrieve(text, intent, topK);
        }
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
