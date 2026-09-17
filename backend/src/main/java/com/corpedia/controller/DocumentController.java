package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.response.DocumentVO;
import com.corpedia.security.UserContext;
import com.corpedia.security.UserContextHolder;
import com.corpedia.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "文档管理", description = "文档上传 / 列表 / 详情 / 删除 / 状态查询")
@RestController
@RequestMapping
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /** 上传文档（multipart，字段 file），立即返回 status=PARSING。 */
    @Operation(summary = "上传文档", description = "multipart 上传（字段名 file），支持 md/pdf/docx/txt；异步入库，立即返回 status=PARSING")
    @PostMapping("/kb/{kbId}/documents")
    public Result<DocumentVO> upload(@Parameter(description = "知识库 id") @PathVariable Long kbId,
                                     @Parameter(description = "文档文件") @RequestParam("file") MultipartFile file) {
        UserContext ctx = UserContextHolder.get();
        return Result.ok(documentService.upload(kbId, file, ctx == null ? null : ctx.userId()));
    }

    /** 知识库下文档列表。 */
    @Operation(summary = "知识库文档列表", description = "返回指定知识库下的全部文档")
    @GetMapping("/kb/{kbId}/documents")
    public Result<List<DocumentVO>> listByKb(@Parameter(description = "知识库 id") @PathVariable Long kbId) {
        return Result.ok(documentService.listByKb(kbId));
    }

    /** 文档详情（轮询端点，硬骨头3）：返回当前 status/chunkCount。 */
    @Operation(summary = "文档详情", description = "入库轮询端点：返回当前 status(PARSING/READY/FAILED) 与 chunkCount")
    @GetMapping("/documents/{id}")
    public Result<DocumentVO> detail(@Parameter(description = "文档 id") @PathVariable Long id) {
        return Result.ok(documentService.getDetail(id));
    }

    /** 批量状态查询（可选）：GET /api/documents?status=PARSING[&kbId=1]。 */
    @Operation(summary = "按状态查询文档", description = "可选按 status(PARSING/READY/FAILED) 与 kbId 过滤")
    @GetMapping("/documents")
    public Result<List<DocumentVO>> listByStatus(@Parameter(description = "文档状态") @RequestParam(required = false) String status,
                                                 @Parameter(description = "知识库 id") @RequestParam(required = false) Long kbId) {
        return Result.ok(documentService.listByStatus(status, kbId));
    }

    /** 删除文档：级联清 Milvus chunk 与磁盘文件。 */
    @Operation(summary = "删除文档", description = "级联清理 Milvus 向量与磁盘文件")
    @DeleteMapping("/documents/{id}")
    public Result<Void> delete(@Parameter(description = "文档 id") @PathVariable Long id) {
        documentService.delete(id);
        return Result.ok();
    }
}
