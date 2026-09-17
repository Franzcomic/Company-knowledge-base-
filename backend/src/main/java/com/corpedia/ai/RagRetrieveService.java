package com.corpedia.ai;

import com.corpedia.common.Constants;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * RAG 检索：query → EmbeddingModel 向量化 → VectorStore.similaritySearch → 片段+相似度（按相似度降序）。
 * 阶段3 为无权限过滤的基础检索；权限 Metadata Filter 注入与 Rerank 在阶段4 增强。
 */
@Service
public class RagRetrieveService {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public RagRetrieveService(EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    /** 检索 topK 个相似片段，按相似度降序返回。 */
    public List<RetrievedChunk> retrieve(String query, int topK) {
        // 显式向量化 query（bge-m3）；VectorStore 内部亦会对 query 向量化，此处保持与检索一致
        embeddingModel.embed(query);
        SearchRequest req = SearchRequest.builder().query(query).topK(topK).build();
        List<Document> hits = vectorStore.similaritySearch(req);
        return hits.stream()
                .map(this::toChunk)
                .sorted(Comparator.comparingDouble(RetrievedChunk::similarity).reversed())
                .toList();
    }

    private RetrievedChunk toChunk(Document d) {
        Object docIdObj = d.getMetadata().get(Constants.META_DOCUMENT_ID);
        Long docId = (docIdObj instanceof Number n) ? n.longValue() : null;
        Object titleObj = d.getMetadata().get(Constants.META_TITLE);
        String title = titleObj == null ? "" : titleObj.toString();
        double score = d.getScore() == null ? 0.0 : d.getScore();
        return new RetrievedChunk(docId, title, d.getId(), d.getText(), score);
    }
}