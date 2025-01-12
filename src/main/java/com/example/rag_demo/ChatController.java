package com.example.rag_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()))
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)) // provide the context data from vector store. it is not going to send the entire pdf only the relevant bits
                .build();
    }

    @GetMapping("/")
    public String chat() {
        return chatClient.prompt()
                .user("How did the federal reserve's recent interest rate cut impact various asset classes according to the analysis")
                .call()
                .content();
    }
}
