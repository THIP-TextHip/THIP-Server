package konkuk.thip.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiAiConfig {
    @Bean
    public ChatClient chatClient(ChatModel geminiModel) {
        return ChatClient.builder(geminiModel).build();
    }
}
