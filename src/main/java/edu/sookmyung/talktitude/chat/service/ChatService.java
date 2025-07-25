package edu.sookmyung.talktitude.chat.service;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.repository.ChatMessageRepository;
import edu.sookmyung.talktitude.config.ai.GPTProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;
    private final GPTProperties gptProperties;

    public List<ChatMessage> findChatMessage(Long sessionId) {
        List<ChatMessage> chatMessage = chatMessageRepository.findByChatSessionId(sessionId);
        return chatMessage;
    }

    //공손 변환 로직
    public String convertToPolite(String originalMessage){
        return generatePoliteMessage(originalMessage);
    }

    public String generatePoliteMessage(String originalMessage){

        GPTProperties.PoliteConfig config = gptProperties.getPolite();
        return chatClient.prompt()
                .user(u->u
                        .text(config.getPolitePrompt()+ originalMessage))
                .options(OpenAiChatOptions.builder()
                        .model(config.getModel())
                        .temperature(config.getTemperature())
                        .maxTokens(config.getMaxTokens())
                        .build())
                .call()
                .content();
    }

}
