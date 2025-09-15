package edu.sookmyung.talktitude.config.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class GptClient {

    private final ObjectMapper om = new ObjectMapper();

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${ai.openai.chat-model:gpt-4o-mini}")
    private String defaultChatModel;

    @Value("${ai.openai.embedding-model:text-embedding-3-small}")
    private String defaultEmbeddingModel;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY not provided");
        }
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        log.info("GptClient initialized. baseUrl={}, chatModel={}, embeddingModel={}",
                baseUrl, defaultChatModel, defaultEmbeddingModel);
    }

    /** 공통 POST 호출 (JSON 반환) */
    private JsonNode post(String path, ObjectNode body) {
        try {
            return webClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("OpenAI error {}: {}", resp.statusCode(), errorBody);
                                return Mono.error(new WebClientResponseException(
                                        "OpenAI error: " + errorBody,
                                        resp.statusCode().value(),
                                        resp.statusCode().toString(),
                                        null, null, null
                                ));
                            })
                    )
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("OpenAI error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BaseException(ErrorCode.GPT_API_FAILED);
        } catch (Exception e) {
            log.error("OpenAI call failed", e);
            throw new BaseException(ErrorCode.GPT_API_FAILED);
        }
    }

    /** Chat Completions */
    public JsonNode chat(JsonNode payload) {
        ObjectNode body = payload.deepCopy();
        if (!body.hasNonNull("model")) {
            body.put("model", defaultChatModel);
        }
        return post("/v1/chat/completions", body);
    }

    /** Embeddings API (payload 직접 전달) */
    public JsonNode embeddings(JsonNode payload) {
        ObjectNode body = payload.deepCopy();
        if (!body.hasNonNull("model")) {
            body.put("model", defaultEmbeddingModel);
        }
        return post("/v1/embeddings", body);
    }

    /** Embeddings API (간편 호출) */
    public JsonNode embeddings(String input) {
        ObjectNode body = om.createObjectNode();
        body.put("model", defaultEmbeddingModel);
        body.putArray("input").add(input == null ? "" : input);
        return post("/v1/embeddings", body);
    }
}