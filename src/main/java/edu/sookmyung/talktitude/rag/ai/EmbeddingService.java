package edu.sookmyung.talktitude.rag.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.sookmyung.talktitude.config.ai.GptClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final GptClient gpt;

    @Value("${ai.openai.embedding-model:text-embedding-3-small}")
    private String embeddingModel;

    public float[] embed(String text) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("model", embeddingModel);
        payload.putArray("input").add(text == null ? "" : text);

        JsonNode res = gpt.embeddings(payload);
        JsonNode arr = res.path("data").get(0).path("embedding");

        int n = arr.size();
        float[] out = new float[n];
        for (int i = 0; i < n; i++) {
            out[i] = (float) arr.get(i).asDouble();
        }
        return out;
    }
}