package com.example.rag_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    @Value("classpath:/prompts/spring-boot-reference.st")
    private Resource resource;

    public ChatController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()))
                //.defaultAdvisors(new QuestionAnswerAdvisor(vectorStore)) // provide the context data from vector store. it is not going to send the entire pdf only the relevant bits
                .build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/")
    public String chat(@RequestParam(value = "prompt") String query) {
        PromptTemplate promptTemplate = new PromptTemplate(resource);
        Map<String, Object> map = new HashMap<>();
        map.put("input", query);
        map.put("documents", String.join("\n", findSimilarDocuments(query)));

        Prompt prompt = promptTemplate.create(map);
//        return chatClient.prompt()
//                .user("How did the federal reserve's recent interest rate cut impact various asset classes according to the analysis")
//                .call()
//                .content();
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    private List<String> findSimilarDocuments(String query) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(2));
        return documents.stream()
                .map(Document::getContent)
                .toList();
    }
}
