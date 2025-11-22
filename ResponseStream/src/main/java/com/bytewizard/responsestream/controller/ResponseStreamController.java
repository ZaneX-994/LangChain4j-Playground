package com.bytewizard.responsestream.controller;

import dev.langchain4j.model.LambdaStreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResponseStreamController {


    @Autowired
    private StreamingChatModel chatModel;

    @GetMapping("/stream/chat1")
    public void streamChat1(){

        String userMessage = "Tell me a joke";

        chatModel.chat(userMessage, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String s) {
                System.out.println("onPartialResponse: " + s);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                System.out.println("onCompleteResponse: " + chatResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError: " + throwable.getMessage());
            }
        });

    }

    @GetMapping("/stream/chat2")
    public void streamChat2() {
        chatModel.chat("Tell me a joke", LambdaStreamingResponseHandler.onPartialResponse(System.out::println));
    }

    @GetMapping("/stream/chat3")
    public void streamChat3() {
        chatModel.chat("Tell me a joke", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
//                process(partialResponse); // 自行处理response
//                if (shouldCancel()) { // 自行判断什么时候取消
//                    context.streamingHandle().cancel();
//                }
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

}
