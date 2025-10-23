package edu.sookmyung.talktitude.chat.rag.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sookmyung.talktitude.chat.rag.ai.EmbeddingService;
import edu.sookmyung.talktitude.chat.rag.model.KbDoc;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class KbIndexerService {
    private final ResourceLoader resourceLoader;
    private final JdbcTemplate jdbc;
    private final EmbeddingService embeddings;
    private final ObjectMapper om = new ObjectMapper();

    public KbIndexerService(ResourceLoader resourceLoader,
                            @Qualifier("ragJdbcTemplate") JdbcTemplate jdbc,
                            EmbeddingService embeddings) {
        this.resourceLoader = resourceLoader;
        this.jdbc = jdbc;
        this.embeddings = embeddings;
    }

    public int reindexFromJsonl(String classpath) throws Exception {
        Resource res = resourceLoader.getResource(classpath);
        List<KbDoc> docs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<?,?> m = om.readValue(line, Map.class);

                String id          = asStr(m.get("id"));
                String category    = asStr(m.get("category"));

                Object tagsObj = m.get("tags");
                List<String> tags = (tagsObj instanceof List<?> l)
                        ? l.stream().map(String::valueOf).filter(s -> !s.isBlank()).toList()
                        : List.of();

                String lastUpdated = asStr(m.get("last_updated"));
                String policy      = asStr(m.get("policy"));
                String script      = asStr(m.get("script"));
                String steps       = String.join("\n", toStrList(m.get("steps")));
                String templates   = String.join("\n", toStrList(m.get("templates")));

                // ⛔ List.of(null, ...) 금지 → Stream.of로 null/blank 제거
                String blob = java.util.stream.Stream.of(policy, script, steps, templates)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(java.util.stream.Collectors.joining("\n"));

                String snippet = summarize(policy, templates);

                docs.add(new KbDoc(id, category, tags, lastUpdated, blob, snippet));
            }
        }

        // 👇 현재 스키마(tags TEXT)에 맞춘 SQL (jsonb 캐스팅 제거)
        String sql = """
            INSERT INTO kb_docs(id, category, tags, last_updated, blob_text, snippet, embedding)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              category    = EXCLUDED.category,
              tags        = EXCLUDED.tags,
              last_updated= EXCLUDED.last_updated,
              blob_text   = EXCLUDED.blob_text,
              snippet     = EXCLUDED.snippet,
              embedding   = EXCLUDED.embedding
            """;

        int count = 0;
        for (KbDoc d : docs) {
            // 임베딩: snippet 우선, 없으면 blob 사용
            String basis = (d.snippet() != null && !d.snippet().isBlank())
                    ? d.snippet() : d.blobText();
            float[] vec = embeddings.embed(basis);

            // pgvector 파라미터
            com.pgvector.PGvector v = new com.pgvector.PGvector(vec);

            // TEXT 저장용 (콤마 구분)
            String tagsCsv = String.join(",", d.tags());

            jdbc.update(sql, ps -> {
                ps.setString(1, d.id());
                ps.setString(2, d.category());
                ps.setString(3, tagsCsv);          // TEXT
                ps.setString(4, d.lastUpdated());
                ps.setString(5, d.blobText());
                ps.setString(6, d.snippet());
                ps.setObject(7, v);                // PGvector
            });
            count++;
        }
        return count;
    }

    private static String summarize(String policy, String templates) {
        String p = policy == null ? "" : policy;
        String t = templates == null ? "" : templates;
        String base = (p + "\n" + t).trim();
        return base.length() > 400 ? base.substring(0, 400) : base;
    }

    private static String asStr(Object o) { return o == null ? null : o.toString(); }

    private static List<String> toStrList(Object o) {
        if (o == null) return List.of();
        if (o instanceof List<?> l) return l.stream().map(Object::toString).toList();
        return List.of(o.toString());
    }
}