package edu.sookmyung.talktitude.rag.search;

import com.pgvector.PGvector;
import edu.sookmyung.talktitude.rag.ai.EmbeddingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PgRetriever {
    private final JdbcTemplate jdbc;
    private final EmbeddingService embeddings;

    public record Row(String id, String category, String snippet) {}

    public PgRetriever(@Qualifier("ragJdbcTemplate") JdbcTemplate jdbc,
                       EmbeddingService embeddings) {
        this.jdbc = jdbc;
        this.embeddings = embeddings;
    }

    public List<Row> retrieve(String text, String intent, int k) {
        float[] q = embeddings.embed(text == null ? "" : text);
        PGvector qv = new PGvector(q);

        // ⬇ 인덱스 ops에 맞춰서 선택 (cosine 인덱스면 <=>)
        String baseSql = """
            SELECT id, category, snippet
            FROM kb_docs
            %s
            ORDER BY embedding <=> ?   -- cosine
            LIMIT ?
            """;
        String where = (intent != null && !intent.isBlank()) ? "WHERE lower(category) = lower(?)" : "";
        String sql = baseSql.formatted(where);

        if (intent != null && !intent.isBlank()) {
            return jdbc.query(sql, new Object[]{intent, qv, k},
                    (rs, i) -> new Row(rs.getString("id"), rs.getString("category"), rs.getString("snippet")));
        } else {
            return jdbc.query(sql, new Object[]{qv, k},
                    (rs, i) -> new Row(rs.getString("id"), rs.getString("category"), rs.getString("snippet")));
        }
    }
}