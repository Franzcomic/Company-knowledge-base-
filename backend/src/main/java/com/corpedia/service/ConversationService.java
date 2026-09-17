package com.corpedia.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.common.BusinessException;
import com.corpedia.common.ResultCode;
import com.corpedia.dto.request.ConversationCreateRequest;
import com.corpedia.dto.response.ConversationVO;
import com.corpedia.dto.response.MessageVO;
import com.corpedia.dto.response.SourceVO;
import com.corpedia.entity.Conversation;
import com.corpedia.entity.Message;
import com.corpedia.mapper.ConversationMapper;
import com.corpedia.mapper.MessageMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public ConversationService(ConversationMapper conversationMapper,
                               MessageMapper messageMapper,
                               ObjectMapper objectMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    /** 当前用户会话列表（按创建时间倒序）。 */
    public List<ConversationVO> list(Long userId) {
        return conversationMapper.selectList(new QueryWrapper<Conversation>()
                        .eq("user_id", userId)
                        .orderByDesc("id"))
                .stream().map(this::toVO).toList();
    }

    public ConversationVO create(Long userId, Long departmentId, ConversationCreateRequest req) {
        Conversation c = new Conversation();
        c.setUserId(userId);
        c.setDepartmentId(departmentId);
        c.setTitle(req != null && req.title() != null && !req.title().isBlank() ? req.title() : null);
        c.setStatus(1);
        conversationMapper.insert(c);
        return toVO(c);
    }

    /** 删除会话及其全部消息。 */
    @Transactional
    public void delete(Long userId, Long id) {
        requireConversation(userId, id);
        messageMapper.delete(new QueryWrapper<Message>().eq("conversation_id", id));
        conversationMapper.deleteById(id);
    }

    /** 会话历史消息（按创建时间升序，贴合对话顺序）。 */
    public List<MessageVO> messages(Long userId, Long id) {
        requireConversation(userId, id);
        return messageMapper.selectList(new QueryWrapper<Message>()
                        .eq("conversation_id", id)
                        .orderByAsc("id"))
                .stream().map(this::toVO).toList();
    }

    public Conversation requireConversation(Long userId, Long id) {
        Conversation c = conversationMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        return c;
    }

    private ConversationVO toVO(Conversation c) {
        return new ConversationVO(c.getId(), c.getTitle(), c.getCreatedAt());
    }

    private MessageVO toVO(Message m) {
        List<SourceVO> sources = null;
        if (m.getSources() != null && !m.getSources().isBlank()) {
            try {
                sources = objectMapper.readValue(m.getSources(), new TypeReference<List<SourceVO>>() {
                });
            } catch (Exception e) {
                sources = List.of();
            }
        }
        Boolean answered = m.getAnswered() == null ? null : m.getAnswered() == 1;
        return new MessageVO(m.getId(), m.getRole(), m.getContent(), sources, answered, m.getCreatedAt());
    }
}