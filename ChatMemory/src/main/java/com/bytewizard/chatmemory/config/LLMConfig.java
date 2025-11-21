package com.bytewizard.chatmemory.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Bean
    public ChatModel createChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("aliQwen_key"))
                .modelName("qwen-long")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
    }

//    @Bean(name = "chatMessageWindowMemory")
//    public ChatAssistantMemory chatMessageWindowMemory(ChatModel chatModel) {
//        return AiServices.builder(ChatAssistantMemory.class)
//                .chatModel(chatModel)
//                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(100))
//                .build();
//    }
//
//    @Bean(name = "chatMessageTokenMemory")
//    public ChatAssistantMemory chatMessageTokenMemory(ChatModel chatModel) {
//
//        TokenCountEstimator openAiTokenCountEstimator = new OpenAiTokenCountEstimator("gpt-4");
//
//        return AiServices.builder(ChatAssistantMemory.class)
//                .chatModel(chatModel)
//                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.withMaxTokens(1000, openAiTokenCountEstimator))
//                .build();
//    }

}

