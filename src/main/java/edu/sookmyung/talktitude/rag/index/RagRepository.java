package edu.sookmyung.talktitude.rag.index;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RagRepository {
    private final JdbcTemplate ragJdbc;
    public RagRepository(org.springframework.jdbc.core.JdbcTemplate ragJdbc) { this.ragJdbc = ragJdbc; }

    public void upsertDoc(String id, String category, String tagsJson, String lastUpdated,
                          String blobText, String snippet, float[] embedding) {

        // vector 캐스팅을 위해 {a,b,c} 문자열 생성 (pgvector)
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        sb.append('}');
        String vec = sb.toString();

        ragJdbc.update("""
            INSERT INTO kb_docs (id, category, tags, last_updated, blob_text, snippet, embedding)
            VALUES (?, ?, ?, ?, ?, ?, ?::vector)
            ON CONFLICT (id) DO UPDATE SET
              category=EXCLUDED.category,
              tags=EXCLUDED.tags,
              last_updated=EXCLUDED.last_updated,
              blob_text=EXCLUDED.blob_text,
              snippet=EXCLUDED.snippet,
              embedding=EXCLUDED.embedding
            """,
                id, category, tagsJson, lastUpdated, blobText, snippet, vec
        );
    }
}