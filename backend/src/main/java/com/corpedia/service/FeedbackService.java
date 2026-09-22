package com.corpedia.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.common.BusinessException;
import com.corpedia.common.ResultCode;
import com.corpedia.dto.request.FeedbackRequest;
import com.corpedia.entity.Conversation;
import com.corpedia.entity.Feedback;
import com.corpedia.entity.Message;
import com.corpedia.mapper.ConversationMapper;
import com.corpedia.mapper.FeedbackMapper;
import com.corpedia.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 回答评价（阶段4 P1）：POST /api/messages/{id}/feedback。
 * 校验消息归属当前用户会话；同一用户对同一消息仅保留一条评价（先清旧再写新）。
 */
@Service
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;

    public FeedbackService(FeedbackMapper feedbackMapper,
                           MessageMapper messageMapper,
                           ConversationMapper conversationMapper) {
        this.feedbackMapper = feedbackMapper;
        this.messageMapper = messageMapper;
        this.conversationMapper = conversationMapper;
    }

    /** 提交评价：校验 rating 与消息归属，同一用户对同一消息先清旧再写新。 */
    public void submit(Long userId, Long messageId, FeedbackRequest req) {
        String rating = req.rating() == null ? "" : req.rating().trim().toUpperCase();
        if (!rating.equals("UP") && !rating.equals("DOWN")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "rating 仅支持 UP / DOWN");
        }
        Message msg = messageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息不存在");
        }
        Conversation conv = conversationMapper.selectById(msg.getConversationId());
        if (conv == null || !conv.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权评价该消息");
        }
        List<Feedback> existing = feedbackMapper.selectList(new QueryWrapper<Feedback>()
                .eq("message_id", messageId).eq("user_id", userId));
        for (Feedback f : existing) {
            feedbackMapper.deleteById(f.getId());
        }
        Feedback fb = new Feedback();
        fb.setMessageId(messageId);
        fb.setUserId(userId);
        fb.setRating(rating);
        fb.setReason(req.reason());
        feedbackMapper.insert(fb);
    }
}
