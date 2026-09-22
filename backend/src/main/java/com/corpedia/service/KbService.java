package com.corpedia.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corpedia.common.BusinessException;
import com.corpedia.common.Constants;
import com.corpedia.common.ResultCode;
import com.corpedia.dto.request.KbCreateRequest;
import com.corpedia.dto.request.KbUpdateRequest;
import com.corpedia.dto.response.KbVO;
import com.corpedia.entity.KbDocument;
import com.corpedia.entity.KnowledgeBase;
import com.corpedia.mapper.KbDocumentMapper;
import com.corpedia.mapper.KnowledgeBaseMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库服务：列表 / 创建 / 更新 / 删除。
 * 列表按当前用户可见范围过滤；写操作校验部门管理权限。
 */
@Service
public class KbService {

    private final KnowledgeBaseMapper kbMapper;
    private final KbDocumentMapper documentMapper;
    private final DocumentService documentService;
    private final ResourceAccessService access;

    public KbService(KnowledgeBaseMapper kbMapper, KbDocumentMapper documentMapper,
                     @Lazy DocumentService documentService, ResourceAccessService access) {
        this.kbMapper = kbMapper;
        this.documentMapper = documentMapper;
        this.documentService = documentService;
        this.access = access;
    }

    /** 返回当前用户可见的知识库列表（按 id 升序）。 */
    public List<KbVO> list() {
        return kbMapper.selectList(new QueryWrapper<KnowledgeBase>().orderByAsc("id"))
                .stream().filter(access::canRead).map(this::toVO).toList();
    }

    /** 创建知识库：校验部门管理权限后落库，权限级别经 normalizeLevel 归一化。 */
    public KbVO create(KbCreateRequest req, Long creatorId) {
        access.requireManage(req.departmentId());
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(req.name());
        kb.setDepartmentId(req.departmentId());
        kb.setDescription(req.description());
        kb.setPermissionLevel(normalizeLevel(req.permissionLevel()));
        kb.setCreatedBy(creatorId);
        kbMapper.insert(kb);
        return toVO(kb);
    }

    /** 更新知识库：仅更新传入字段；修改部门时需同时具备新旧部门管理权限。 */
    @Transactional
    public KbVO update(Long id, KbUpdateRequest req) {
        KnowledgeBase kb = requireKb(id);
        access.requireManage(kb.getDepartmentId());
        if (req.departmentId() != null) access.requireManage(req.departmentId());
        if (req.name() != null && !req.name().isBlank()) {
            kb.setName(req.name());
        }
        if (req.departmentId() != null) {
            kb.setDepartmentId(req.departmentId());
        }
        if (req.permissionLevel() != null && !req.permissionLevel().isBlank()) {
            kb.setPermissionLevel(normalizeLevel(req.permissionLevel()));
        }
        if (req.description() != null) {
            kb.setDescription(req.description());
        }
        kbMapper.updateById(kb);
        return toVO(kb);
    }

    /** 删除知识库：级联删除其下文档（文件 + Milvus chunk + 行）。 */
    @Transactional
    public void delete(Long id) {
        access.requireManage(requireKb(id).getDepartmentId());
        List<KbDocument> docs = documentMapper.selectList(
                new QueryWrapper<KbDocument>().eq("kb_id", id));
        for (KbDocument doc : docs) {
            documentService.delete(doc.getId());
        }
        kbMapper.deleteById(id);
    }

    /** 读取并校验可见性：不存在抛 404，无权读取抛 403。 */
    public KnowledgeBase requireKb(Long id) {
        KnowledgeBase kb = kbMapper.selectById(id);
        if (kb == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识库不存在");
        }
        access.requireRead(kb);
        return kb;
    }

    /** 归一化权限级别：空值缺省 PUBLIC；非法值抛 400。 */
    private String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            return Constants.LV_PUBLIC;
        }
        String up = level.trim().toUpperCase();
        if (!up.equals(Constants.LV_PUBLIC) && !up.equals(Constants.LV_DEPT) && !up.equals(Constants.LV_CONFIDENTIAL)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "permissionLevel 仅支持 PUBLIC/DEPT/CONFIDENTIAL");
        }
        return up;
    }

    private KbVO toVO(KnowledgeBase kb) {
        return new KbVO(kb.getId(), kb.getName(), kb.getDepartmentId(),
                kb.getPermissionLevel(), kb.getDescription(), kb.getCreatedAt());
    }
}
