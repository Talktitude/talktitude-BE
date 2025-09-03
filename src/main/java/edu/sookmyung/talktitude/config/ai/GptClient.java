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

    private static final String DEFAULT_CHAT_MODEL = "gpt-3.5-turbo";
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

        log.info("GptClient initialized. baseUrl={}, model={}", baseUrl, DEFAULT_CHAT_MODEL);
    }

    public JsonNode chat(JsonNode payload) {
        try {
            ObjectNode body = payload.deepCopy();
            if (!body.hasNonNull("model")) {
                body.put("model", DEFAULT_CHAT_MODEL);
            }

            return webClient.post()
                    .uri("/v1/chat/completions")
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
                    // ✅ 여기만 변경: 문자열이 아니라 JsonNode로 바로 받기
                    .bodyToMono(com.fasterxml.jackson.databind.JsonNode.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("OpenAI error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BaseException(ErrorCode.GPT_API_FAILED);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.GPT_API_FAILED);
        }
    }
}