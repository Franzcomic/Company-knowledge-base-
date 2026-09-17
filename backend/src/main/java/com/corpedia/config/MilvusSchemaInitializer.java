package com.corpedia.config;

import com.corpedia.common.Constants;
import com.corpedia.dto.response.DocumentChunkVO;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.QueryResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 硬骨头 2：确保集合 knowledge_chunks 存在（Spring AI 兼容 schema），
 * 并尽力创建权限过滤所需 JSON 子字段索引（department_id / permission_level）。
 * 若索引创建失败（本地 Milvus 兼容问题）仅告警不阻断——过滤功能本身不依赖索引。
 * 同时提供按 document_id 表达式删除（文档删除级联清 Milvus）。
 */
@Component
public class MilvusSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MilvusSchemaInitializer.class);

    private final MilvusClientV2 client;
    private final RagProperties rag;
    @Value("${spring.ai.vectorstore.milvus.embedding-dimension:1024}")
    private int embeddingDimension;

    public MilvusSchemaInitializer(MilvusClientV2 client, RagProperties rag) {
        this.client = client;
        this.rag = rag;
    }

    @Override
    public void run(ApplicationArguments args) {
        String collection = rag.getCollection();
        try {
            boolean exists = client.listCollections().getCollectionNames().contains(collection);
            if (!exists) {
                createCollection(collection);
            }
            ensureIndexes(collection);
            log.info("[MilvusSchemaInitializer] 集合 {} 就绪（硬骨头2：JSON 子字段过滤元数据）", collection);
        } catch (Exception e) {
            log.warn("[MilvusSchemaInitializer] 集合初始化异常（后续 add 时 Spring AI 会兜底建集）: {}", e.getMessage(), e);
        }
    }

    /** 手动建集，schema 与 Spring AI MilvusVectorStore 默认一致（doc_id/content/metadata/embedding）。 */
    private void createCollection(String collection) {
        CreateCollectionReq.CollectionSchema schema = client.createSchema();
        schema.addField(AddFieldReq.builder()
                .fieldName("doc_id").dataType(DataType.VarChar).isPrimaryKey(true).maxLength(64).build());
        schema.addField(AddFieldReq.builder()
                .fieldName("content").dataType(DataType.VarChar).maxLength(65535).build());
        schema.addField(AddFieldReq.builder()
                .fieldName("metadata").dataType(DataType.JSON).build());
        schema.addField(AddFieldReq.builder()
                .fieldName("embedding").dataType(DataType.FloatVector).dimension(embeddingDimension).build());
        client.createCollection(CreateCollectionReq.builder()
                .collectionName(collection)
                .dimension(embeddingDimension)
                .metricType(IndexParam.MetricType.COSINE.name())
                .primaryFieldName("doc_id")
                .vectorFieldName("embedding")
                .collectionSchema(schema)
                .build());
        // 向量索引（AUTOINDEX 让 Milvus 自动选型）
        client.createIndex(CreateIndexReq.builder()
                .collectionName(collection)
                .indexParams(List.of(IndexParam.builder()
                        .fieldName("embedding")
                        .indexType(IndexParam.IndexType.AUTOINDEX)
                        .metricType(IndexParam.MetricType.COSINE)
                        .build()))
                .build());
        client.loadCollection(LoadCollectionReq.builder().collectionName(collection).build());
        log.info("[MilvusSchemaInitializer] 已创建集合 {}（Spring AI 兼容 schema）", collection);
    }

    /** JSON 子字段索引：权限过滤列 department_id / permission_level（数值，无 null 语义）。
     *  官方写法：fieldName 为顶层 JSON 字段，路径经 extraParams["json_path"] 指定，
     *  数值 cast 用 DOUBLE，AUTOINDEX 支持等值 in/范围 <=。 */
    private void ensureIndexes(String collection) {
        tryCreateJsonIndex(collection, "metadata[\"" + Constants.META_DEPARTMENT_ID + "\"]", "idx_department_id");
        tryCreateJsonIndex(collection, "metadata[\"" + Constants.META_PERMISSION_LEVEL + "\"]", "idx_permission_level");
    }

    private void tryCreateJsonIndex(String collection, String jsonPath, String indexName) {
        try {
            client.createIndex(CreateIndexReq.builder()
                    .collectionName(collection)
                    .indexParams(List.of(IndexParam.builder()
                            .fieldName("metadata")
                            .indexName(indexName)
                            .indexType(IndexParam.IndexType.AUTOINDEX)
                            .extraParams(Map.of(
                                    "json_path", jsonPath,
                                    "json_cast_type", "DOUBLE"))
                            .build()))
                    .build());
            log.info("[MilvusSchemaInitializer] 已创建 JSON 子字段索引 {}", indexName);
        } catch (Exception e) {
            log.warn("[MilvusSchemaInitializer] JSON 子字段索引 {} 创建失败（过滤仍可用，仅性能影响）: {}",
                    indexName, e.getMessage());
        }
    }

    /** 删除某文档的全部 chunk：metadata["document_id"] == {documentId}。 */
    public void deleteByDocumentId(Long documentId) {
        try {
            client.delete(DeleteReq.builder()
                    .collectionName(rag.getCollection())
                    .filter("metadata[\"" + Constants.META_DOCUMENT_ID + "\"] == " + documentId)
                    .build());
            log.info("[MilvusSchemaInitializer] 已删除 documentId={} 的 Milvus chunk", documentId);
        } catch (Exception e) {
            log.warn("[MilvusSchemaInitializer] 删除 documentId={} 的 chunk 失败: {}", documentId, e.getMessage());
            throw new IllegalStateException("向量删除失败，请重试", e);
        }
    }

    /**
     * 查询某文档的全部分块（GET /api/documents/{id}/chunks 数据源）。
     * 返回按 chunk_index 升序的 [chunkIndex, content]；similarity 无检索 query 不可得，置 null。
     */
    public List<DocumentChunkVO> queryChunksByDocumentId(Long documentId) {
        List<DocumentChunkVO> out = new ArrayList<>();
        try {
            QueryResp resp = client.query(QueryReq.builder()
                    .collectionName(rag.getCollection())
                    .filter("metadata[\"" + Constants.META_DOCUMENT_ID + "\"] == " + documentId)
                    .outputFields(List.of("doc_id", "content"))
                    .build());
            for (QueryResp.QueryResult row : resp.getQueryResults()) {
                Map<String, Object> entity = row.getEntity();
                String chunkId = entity.get("doc_id") == null ? null : entity.get("doc_id").toString();
                String content = entity.get("content") == null ? null : entity.get("content").toString();
                if (content == null) {
                    continue;
                }
                out.add(new DocumentChunkVO(parseChunkIndex(chunkId), content, null));
            }
            out.sort(Comparator.comparingInt(DocumentChunkVO::chunkIndex));
        } catch (Exception e) {
            log.warn("[MilvusSchemaInitializer] 查询 documentId={} 的 chunks 失败: {}", documentId, e.getMessage());
        }
        return out;
    }

    /** 从确定性 chunkId（doc-{documentId}-{idx}）解析分块序号。 */
    private Integer parseChunkIndex(String chunkId) {
        if (chunkId == null) {
            return null;
        }
        int idx = chunkId.lastIndexOf('-');
        if (idx < 0 || idx == chunkId.length() - 1) {
            return null;
        }
        try {
            return Integer.parseInt(chunkId.substring(idx + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
