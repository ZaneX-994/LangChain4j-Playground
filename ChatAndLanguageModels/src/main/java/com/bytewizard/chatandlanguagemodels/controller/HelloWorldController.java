package com.bytewizard.chatandlanguagemodels.controller;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/hello")
public class HelloWorldController {

    @Autowired
    private ChatModel model;

    @Value("classpath:static/images/img.png")
    private Resource resource;

    @GetMapping()
    public String sayHello() {
        return model.chat("Hello World");
    }

    @GetMapping("/request")
    public String sayHelloWithChatRequest() {
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("你是谁"))
                .build();

        ChatResponse response = model.chat(request);

        return response.aiMessage().text();
    }

    @GetMapping("/message")
    public String sayHelloWithChatMessage() {
        UserMessage firstUserMessage = UserMessage.from("Hello, my name is Klaus");
        AiMessage firstAiMessage = model.chat(firstUserMessage).aiMessage(); // Hi Klaus, how can I help you?
        UserMessage secondUserMessage = UserMessage.from("What is my name?");
        AiMessage secondAiMessage = model.chat(firstUserMessage, firstAiMessage, secondUserMessage).aiMessage(); // Klaus

        return secondAiMessage.text();
    }

    @GetMapping("/image")
    public String imageDescription() throws IOException {

        byte[] byteArray = resource.getContentAsByteArray();
        String base64Data = Base64.getEncoder().encodeToString(byteArray);

        // 提示词指定
        ImageContent from = ImageContent.from(base64Data, "image/png");
        UserMessage message = UserMessage.from(
                TextContent.from("描述图片内容"),
                ImageContent.from(base64Data, "image/png")
        );

        ChatResponse response = model.chat(message);
        return response.aiMessage().text();
    }

}
