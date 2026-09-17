package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.request.FeedbackRequest;
import com.corpedia.security.UserContextHolder;
import com.corpedia.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回答评价（阶段4 P1）。
 */
@Tag(name = "消息问答", description = "回答评价（赞/踩 + 可选原因）")
@RestController
@RequestMapping("/messages")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @Operation(summary = "评价回答", description = "rating=UP/DOWN + 可选 reason；同一用户对同一消息重复评价会覆盖")
    @PostMapping("/{messageId}/feedback")
    public Result<Void> submit(@Parameter(description = "assistant 消息 id") @PathVariable Long messageId,
                               @Valid @RequestBody FeedbackRequest req) {
        feedbackService.submit(UserContextHolder.get().userId(), messageId, req);
        return Result.ok();
    }
}
