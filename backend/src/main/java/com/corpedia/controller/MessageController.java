package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.request.SendMessageRequest;
import com.corpedia.dto.response.SendMessageResultVO;
import com.corpedia.security.UserContextHolder;
import com.corpedia.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "消息问答", description = "提问 → 检索 → 生成 → 落库 → 返回（RAG 主链路）")
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "提问并回答")
    @PostMapping
    public Result<SendMessageResultVO> send(@Valid @RequestBody SendMessageRequest req) {
        return Result.ok(messageService.ask(UserContextHolder.get().userId(),
                req.conversationId(), req.content()));
    }
}