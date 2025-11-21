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


### 2. ChatMemory

手动维护和管理聊天消息很麻烦。因此，LangChain4j 提供了 ChatMemory 抽象层以及多个开箱即用的实现。

ChatMemory 当作ChatMessages的容器, 并提供以下功能:

- Eviction policy

- Persistence

- Special treatment of SystemMessage

- Special treatment of tool messages

  

#### 记忆和历史的区别

- 历史记录会完整保存用户与人工智能之间的所有消息。用户在用户界面中看到的就是历史记录，它代表了实际发生过的所有对话内容。
- 记忆会保留一些展示给大模型使得它仿佛记得回话. 根据所使用的记忆算法，它可以以各种方式修改历史记录: 驱逐一些消息、汇总多个消息、汇总单独的消息、从消息中删除不重要的细节、向消息中注入额外信息（例如 RAG）或指令（例如 结构化输出），等等。

LangChain4j 目前只提供记忆



#### Eviction Policy

##### 为什么需要:

- 为了适应 LLM 的上下文窗口，LLM 一次可以处理的令牌数量是有上限的。在某些情况下，对话可能会超过这个限制。在这种情况下，应该移除一些消息。通常情况下，会移除最旧的消息，但如有必要，也可以实现更复杂的算法。
- 为了控制成本。每个令牌都有成本，因此每次调用 LLM 的成本都会逐渐增加。清除不必要的消息可以降低成本。
- 为了控制延迟。发送到 LLM 的令牌越多，处理它们所需的时间就越长。

##### 目前LangChain4j提供两种实现:

1. **MessageWindowChatMemory**: 保留最近的N条消息的滑动窗口 (由于每条消息可以包含数量不等的令牌，因此 MessageWindowChatMemory 主要用于快速原型设计)
2. **TokenWindowChatMemory**: 保留最近的N个token



#### Persistence

ChatMemory 的实现默认把ChatMessages存在内存, 如果对持久化有需求则可以自定义ChatMemoryStore区存储ChatMessages:

class PersistentChatMemoryStore implements ChatMemoryStore {

```Java
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
      // TODO: Implement getting all messages from the persistent store by memory ID.
      // ChatMessageDeserializer.messageFromJson(String) and 
      // ChatMessageDeserializer.messagesFromJson(String) helper methods can be used to
      // easily deserialize chat messages from JSON.
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // TODO: Implement updating all messages in the persistent store by memory ID.
        // ChatMessageSerializer.messageToJson(ChatMessage) and 
        // ChatMessageSerializer.messagesToJson(List<ChatMessage>) helper methods can be used to
        // easily serialize chat messages into JSON.
    }

    @Override
    public void deleteMessages(Object memoryId) {
      // TODO: Implement deleting all messages in the persistent store by memory ID.
    }
}

ChatMemory chatMemory = MessageWindowChatMemory.builder()
        .id("12345")
        .maxMessages(10)
        .chatMemoryStore(new PersistentChatMemoryStore())
        .build();
```





**MessageWindowChatMemory:**



```Java
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
```



**TokenWindowChatMemory:**

```Java
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
```

#### 系统消息的特殊处理

SystemMessage 是一种特殊类型的消息，因此其处理方式与其他消息类型不同：

- SystemMessage 一旦添加，便会一直保留
- 同一时间只能存在一条 SystemMessage。
- 如果添加一条内容相同的 SystemMessage，则会被忽略。
- 如果添加一条内容不同的 SystemMessage，则会替换之前添加的 SystemMessage。





 