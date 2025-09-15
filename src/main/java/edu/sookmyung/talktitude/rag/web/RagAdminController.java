package edu.sookmyung.talktitude.rag.web;

import edu.sookmyung.talktitude.rag.index.KbIndexerService;
import edu.sookmyung.talktitude.rag.search.PgRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/rag")
@RequiredArgsConstructor
public class RagAdminController {

    private final KbIndexerService indexer;
    private final PgRetriever pg;

    // application.properties의 recommend.kb.path 값을 기본값으로 사용
    @Value("${recommend.kb.path:classpath:kb/cs_kb.jsonl}")
    private String defaultKbPath;

    /**
     * 색인 실행 (경로 미지정 시 기본값 사용)
     */
    @PostMapping("/reindex")
    public Map<String, Object> reindex(@RequestParam(value = "path", required = false) String path) throws Exception {
        String p = (path == null || path.isBlank()) ? defaultKbPath : path;
        int n = indexer.reindexFromJsonl(p);
        return Map.of("reindexed", n, "path", p);
    }

    /**
     * RAG 검색 검증용
     */
    @GetMapping("/search")
    public List<PgRetriever.Row> search(
            @RequestParam String text,
            @RequestParam(required = false) String intent,
            @RequestParam(defaultValue = "3") int k
    ) {
        return pg.retrieve(text, intent, k);
    }
}

