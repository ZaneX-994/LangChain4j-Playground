## LangChain4j



### 1. Chat and Language Models

#### LLMs 目前有两种API类型:

##### 1. LanguageModels: 它的API比较简单, 只接收字符串并输出字符串 (LangChain4j不再扩展支持)

##### 2. ChatModels: 接收一个或多个ChatMessage并返回一个AiMessage (取决于选择的大模型, ChatMessage 可以包含文字, 图片, 音频等)

除了以上两种模型, LangChain4j还支持:

- EmbeddingModel - 将文字向量化
- ImageModel - 生成/编辑图片
- ModerationModel - 检查文本是否包含有害内容
- ScoringModel - 可以根据查询对多段文本进行评分（或排名），本质上是确定每段文本与查询的相关性。这对于 RAG非常有用。这些内容将在后面详细介绍。

##### ChatModel 包含多种chat方法

```Java
public interface ChatModel {
    ...
    
    String chat(String userMessage);
  
	  ChatResponse chat(ChatRequest chatRequest);
  
    ChatResponse chat(ChatMessage... messages);

    ChatResponse chat(List<ChatMessage> messages);
        
    ...
}
```

**ChatResponse 包含AiMessage和ChatResponseMetadata, 其中ChatResponseMetadata含有TokenUsage信息 - 你提供给大模型的输入包含的令牌数和生成的令牌数**

1. String chat(String userMessage);

   ```Java
   @GetMapping()
   public String sayHello() {
       return model.chat("Hello World");
   }
   ```

   

2. ChatResponse chat(ChatRequest chatRequest);

   ```Java
   @GetMapping("/request")
   public String sayHelloWithChatRequest() {
       ChatRequest request = ChatRequest.builder()
               .messages(UserMessage.from("你是谁"))
               .build();
   
       ChatResponse response = model.chat(request);
   
       return response.aiMessage().text();
   }
   ```

   

3. ChatResponse chat(ChatMessage... messages);

   ```Java
   @GetMapping("/message")
   public String sayHelloWithChatMessage() {
       UserMessage firstUserMessage = UserMessage.from("Hello, my name is Klaus");
       AiMessage firstAiMessage = model.chat(firstUserMessage).aiMessage(); // Hi Klaus, how can I help you?
       UserMessage secondUserMessage = UserMessage.from("What is my name?");
       AiMessage secondAiMessage = model.chat(firstUserMessage, firstAiMessage, secondUserMessage).aiMessage(); // Klaus
   
       return secondAiMessage.text();
   }
   ```

   UserMessage 可以包含content列表: List<Content> contents, 其中Content接口有多个实现类:

   - TextContent
   - ImageContent
   - AudioContent
   - VideoContent
   - PdfFileContent

   图像+文字: <font color="red">**(注意使用支持图片格式的大模型, 如qwen-vl-max)**</font>

   ```Java
   @Value("classpath:static/images/img.png")
   private Resource resource;
   
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
   ```

   

   

   

 