package com.corpedia.service;

import com.corpedia.common.Constants;
import com.corpedia.entity.KbDocument;
import com.corpedia.entity.KnowledgeBase;
import com.corpedia.mapper.KbDocumentMapper;
import com.corpedia.mapper.KnowledgeBaseMapper;
import com.corpedia.security.UserContext;
import com.corpedia.security.UserContextHolder;
import org.springframework.stereotype.Service;
import java.util.Objects;

/** 同一套规则用于列表、按 ID 读取、写操作以及送入模型前的来源复核。 */
@Service
public class ResourceAccessService {
    private final PermissionService permissions;
    private final KbDocumentMapper documents;
    private final KnowledgeBaseMapper knowledgeBases;

    public ResourceAccessService(PermissionService permissions, KbDocumentMapper documents,
                                 KnowledgeBaseMapper knowledgeBases) {
        this.permissions = permissions;
        this.documents = documents;
        this.knowledgeBases = knowledgeBases;
    }

    public boolean canRead(Long departmentId, String level) {
        UserContext user = UserContextHolder.get();
        if (user == null) return false;
        if (PermissionService.ROLE_SYS_ADMIN.equals(user.roleCode())) return true;
        if (!PermissionService.ROLE_EMPLOYEE.equals(user.roleCode()) &&
                !PermissionService.ROLE_DEPT_ADMIN.equals(user.roleCode())) return false;
        boolean scope = departmentId == null || departmentId == 0 || Objects.equals(departmentId, user.deptId());
        if (!scope) return false;
        if (Constants.LV_PUBLIC.equals(level)) return true;
        if (Constants.LV_DEPT.equals(level)) return departmentId != null && departmentId != 0 &&
                Objects.equals(departmentId, user.deptId());
        return Constants.LV_CONFIDENTIAL.equals(level) &&
                PermissionService.ROLE_DEPT_ADMIN.equals(user.roleCode()) &&
                departmentId != null && departmentId != 0 && Objects.equals(departmentId, user.deptId());
    }

    public boolean canRead(KnowledgeBase kb) {
        return kb != null && canRead(kb.getDepartmentId(), kb.getPermissionLevel());
    }

    public boolean canRead(KbDocument document) {
        return document != null && canRead(knowledgeBases.selectById(document.getKbId())) &&
                canRead(document.getDepartmentId(), document.getPermissionLevel());
    }

    public boolean canUseSource(Long documentId) {
        if (documentId == null) return false;
        KbDocument doc = documents.selectById(documentId);
        return doc != null && Constants.DOC_READY.equals(doc.getStatus()) && canRead(doc);
    }

    public void requireRead(KnowledgeBase kb) {
        if (!canRead(kb)) throw permissions.forbidden();
    }

    public void requireRead(KbDocument doc) {
        if (!canRead(doc)) throw permissions.forbidden();
    }

    public void requireManage(Long departmentId) {
        UserContext user = UserContextHolder.get();
        permissions.requireDocManage(user);
        if (!PermissionService.ROLE_SYS_ADMIN.equals(user.roleCode()) &&
                (departmentId == null || departmentId == 0 || !Objects.equals(departmentId, user.deptId()))) {
            throw permissions.forbidden();
        }
    }
}
