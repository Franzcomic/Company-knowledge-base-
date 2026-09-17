package com.corpedia.controller;

import com.corpedia.ai.DocumentPipelineService;
import com.corpedia.common.BusinessException;
import com.corpedia.common.Constants;
import com.corpedia.common.Result;
import com.corpedia.common.ResultCode;
import com.corpedia.dto.response.ProcessResultVO;
import com.corpedia.entity.KbDocument;
import com.corpedia.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文档入库管道", description = "触发 / 重试文档解析入库（异步）")
@RestController
@RequestMapping("/rag")
public class RagController {

    private final DocumentPipelineService pipeline;
    private final DocumentService documentService;

    public RagController(DocumentPipelineService pipeline, DocumentService documentService) {
        this.pipeline = pipeline;
        this.documentService = documentService;
    }

    /** 触发/重试文档入库管道：立即返回 PARSING，处理异步执行。 */
    @Operation(summary = "触发文档入库", description = "对 PARSING/FAILED 文档触发/重试异步入库；READY 文档返回 400")
    @PostMapping("/process/{documentId}")
    public Result<ProcessResultVO> process(@Parameter(description = "文档 id") @PathVariable Long documentId) {
        KbDocument doc = documentService.requireDoc(documentId);
        if (Constants.DOC_READY.equals(doc.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文档已入库完成，无需重复处理");
        }
        documentService.reprocess(documentId);
        return Result.ok(new ProcessResultVO(documentId, Constants.DOC_PARSING));
    }
}
