package com.corpedia.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.ai.DocumentPipelineService;
import com.corpedia.common.BusinessException;
import com.corpedia.common.Constants;
import com.corpedia.common.ResultCode;
import com.corpedia.config.MilvusSchemaInitializer;
import com.corpedia.config.StorageProperties;
import com.corpedia.dto.response.DocumentVO;
import com.corpedia.entity.KbDocument;
import com.corpedia.entity.KnowledgeBase;
import com.corpedia.mapper.KbDocumentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_TYPES = Set.of("md", "pdf", "docx", "txt");

    private final KbDocumentMapper documentMapper;
    private final KbService kbService;
    private final DocumentPipelineService pipeline;
    private final MilvusSchemaInitializer milvus;
    private final StorageProperties storage;

    public DocumentService(KbDocumentMapper documentMapper,
                           KbService kbService,
                           DocumentPipelineService pipeline,
                           MilvusSchemaInitializer milvus,
                           StorageProperties storage) {
        this.documentMapper = documentMapper;
        this.kbService = kbService;
        this.pipeline = pipeline;
        this.milvus = milvus;
        this.storage = storage;
    }

    /** 上传：落盘 + 插入 PARSING 行 + 触发异步管道。 */
    public DocumentVO upload(Long kbId, MultipartFile file, Long uploaderId) {
        KnowledgeBase kb = kbService.requireKb(kbId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不能为空");
        }
        String ext = extensionOf(filename);
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 md/pdf/docx/txt 格式");
        }
        try {
            Path dir = Path.of(storage.getPath(), String.valueOf(kbId));
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID().toString().replace("-", "") + "_" + filename;
            Path target = dir.resolve(storedName).normalize();
            file.transferTo(target.toAbsolutePath());

            KbDocument doc = new KbDocument();
            doc.setKbId(kbId);
            doc.setFilename(filename);
            doc.setFilePath(target.toAbsolutePath().toString());
            doc.setFileType(ext);
            doc.setSize(file.getSize());
            doc.setStatus(Constants.DOC_PARSING);
            doc.setChunkCount(0);
            doc.setPermissionLevel(kb.getPermissionLevel() == null ? Constants.LV_PUBLIC : kb.getPermissionLevel());
            doc.setUploadedBy(uploaderId);
            documentMapper.insert(doc);

            pipeline.ingest(doc.getId());   // 异步处理，立即返回 PARSING
            return toVO(doc);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.ERROR, "文件保存失败: " + e.getMessage());
        }
    }

    public List<DocumentVO> listByKb(Long kbId) {
        return documentMapper.selectList(new QueryWrapper<KbDocument>()
                        .eq("kb_id", kbId).orderByDesc("id"))
                .stream().map(this::toVO).toList();
    }

    /** 轮询/详情端点（硬骨头3）：返回当前 status / chunkCount。 */
    public DocumentVO getDetail(Long id) {
        return toVO(requireDoc(id));
    }

    /** 批量状态查询（可选）：GET /api/documents?status=PARSING。 */
    public List<DocumentVO> listByStatus(String status, Long kbId) {
        QueryWrapper<KbDocument> qw = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            qw.eq("status", status.toUpperCase());
        }
        if (kbId != null) {
            qw.eq("kb_id", kbId);
        }
        qw.orderByDesc("id");
        return documentMapper.selectList(qw).stream().map(this::toVO).toList();
    }

    /** 删除文档：先清 Milvus chunk，再删文件与行。 */
    @Transactional
    public void delete(Long id) {
        KbDocument doc = requireDoc(id);
        milvus.deleteByDocumentId(id);
        try {
            Files.deleteIfExists(Path.of(doc.getFilePath()));
        } catch (IOException e) {
            // 文件缺失/占用不影响主流程，仅记录
        }
        documentMapper.deleteById(id);
    }

    public KbDocument requireDoc(Long id) {
        KbDocument doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
        }
        return doc;
    }

    private String extensionOf(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx < 0 ? "" : filename.substring(idx + 1).toLowerCase();
    }

    private DocumentVO toVO(KbDocument d) {
        return new DocumentVO(d.getId(), d.getKbId(), d.getFilename(), d.getFileType(), d.getSize(),
                d.getStatus(), d.getChunkCount(), d.getPermissionLevel(), d.getCreatedAt());
    }
}
