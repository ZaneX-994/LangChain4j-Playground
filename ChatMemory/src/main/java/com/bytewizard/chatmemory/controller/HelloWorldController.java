package com.bytewizard.chatmemory.controller;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
public class HelloWorldController {

    @Autowired
    private ChatModel model;


    @GetMapping("/chatmemory/test1")
    public String chat1() {

        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .id("12345")
                .maxMessages(10)
                .build();
        UserMessage firstUserMessage = UserMessage.from("Hello, my name is Klaus");
        UserMessage secondUserMessage = UserMessage.from("What is my name?");
        memory.add(firstUserMessage);
        memory.add(secondUserMessage);

        ChatResponse response = model.chat(memory.messages());

        return response.aiMessage().text();
    }

    @GetMapping("/chatmemory/test2")
    public String chat2() {
        TokenWindowChatMemory memory = TokenWindowChatMemory.builder()
                .id("123456")
                .maxTokens(100, new OpenAiTokenCountEstimator("gpt-4"))
                .build();
        UserMessage firstUserMessage = UserMessage.from("Hello, my name is Bytewizard");
        UserMessage secondUserMessage = UserMessage.from("What is my name?");

        memory.add(firstUserMessage);
        memory.add(secondUserMessage);

        ChatResponse response = model.chat(memory.messages());

        return response.aiMessage().text();
    }

//    @GetMapping("/request")
//    public String sayHelloWithChatRequest() {
//        ChatRequest request = ChatRequest.builder()
//                .messages(UserMessage.from("你是谁"))
//                .build();
//
//        ChatResponse response = model.chat(request);
//
//        return response.aiMessage().text();
//    }
//

//


}
