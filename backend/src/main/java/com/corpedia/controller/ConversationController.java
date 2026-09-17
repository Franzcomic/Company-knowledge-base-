package com.corpedia.controller;

import com.corpedia.common.Result;
import com.corpedia.dto.request.ConversationCreateRequest;
import com.corpedia.dto.response.ConversationVO;
import com.corpedia.dto.response.MessageVO;
import com.corpedia.security.UserContext;
import com.corpedia.security.UserContextHolder;
import com.corpedia.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "会话管理", description = "会话列表 / 新建 / 删除 / 历史消息")
@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @Operation(summary = "我的会话列表")
    @GetMapping
    public Result<List<ConversationVO>> list() {
        return Result.ok(conversationService.list(currentUserId()));
    }

    @Operation(summary = "新建会话")
    @PostMapping
    public Result<ConversationVO> create(@Valid @RequestBody(required = false) ConversationCreateRequest req) {
        UserContext ctx = UserContextHolder.get();
        return Result.ok(conversationService.create(ctx.userId(), ctx.deptId(), req));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        conversationService.delete(currentUserId(), id);
        return Result.ok();
    }

    @Operation(summary = "会话历史消息")
    @GetMapping("/{id}/messages")
    public Result<List<MessageVO>> messages(@Parameter(description = "会话 id") @PathVariable Long id) {
        return Result.ok(conversationService.messages(currentUserId(), id));
    }

    private Long currentUserId() {
        return UserContextHolder.get().userId();
    }
}