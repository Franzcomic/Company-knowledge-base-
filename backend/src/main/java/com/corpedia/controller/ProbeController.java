package com.corpedia.controller;

import com.corpedia.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 冒烟探针(硬骨头1): 验证 LM Studio chat / bge-m3 embedding(1024维) / Milvus 连通。
 */
@Tag(name = "AI 冒烟探针", description = "硬骨头1 验证：LM Studio chat / bge-m3 embedding(1024维) / Milvus 连通")
@RestController
@RequestMapping("/probe")
public class ProbeController {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public ProbeController(ChatClient.Builder chatClientBuilder,
                           EmbeddingModel embeddingModel,
                           VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    @Operation(summary = "Chat 连通探针", description = "调用 LM Studio(gemma-4-e4b) 返回一句自我介绍")
    @GetMapping("/chat")
    public Result<String> chat() {
        String answer = chatClient.prompt("请用一句话自我介绍")
                .call().content();
        return Result.ok(answer);
    }

    @Operation(summary = "Embedding 探针", description = "返回 bge-m3 向量维度（应为 1024）")
    @GetMapping("/embedding")
    public Result<Map<String, Object>> embedding() {
        float[] vec = embeddingModel.embed("企业内部知识库问答测试");
        return Result.ok(Map.of(
                "dimension", vec.length,
                "assert1024", vec.length == 1024,
                "sample", vec.length > 0 ? vec[0] : 0
        ));
    }

    @Operation(summary = "Milvus 探针", description = "真实写入一条并检索，验证 Milvus v3 与 Spring AI VectorStore 连通")
    @GetMapping("/milvus")
    public Result<Map<String, Object>> milvus() {
        // 真实写入+检索, 验证 Milvus v3 与 Spring AI VectorStore 连通(硬骨头1/2 前置)
        String text = "企业内部知识库问答测试片段, 验证 Milvus 连通性";
        Document doc = new Document(UUID.randomUUID().toString(), text, Map.of("probe", "true"));
        vectorStore.add(List.of(doc));
        SearchRequest req = SearchRequest.builder().query("知识库问答").topK(3).build();
        List<Document> hits = vectorStore.similaritySearch(req);
        return Result.ok(Map.of(
                "add", "ok",
                "hits", hits.size(),
                "topTexts", hits.stream().map(h -> h.getText().length()).toList()
        ));
    }
}