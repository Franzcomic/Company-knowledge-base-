package com.corpedia.service;

import com.corpedia.common.BusinessException;
import com.corpedia.entity.KbDocument;
import com.corpedia.entity.KnowledgeBase;
import com.corpedia.mapper.*;
import com.corpedia.security.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceAccessServiceTest {
    private final KbDocumentMapper docs = mock(KbDocumentMapper.class);
    private final KnowledgeBaseMapper kbs = mock(KnowledgeBaseMapper.class);
    private final ResourceAccessService access = new ResourceAccessService(
            new PermissionService(mock(UserMapper.class), mock(RoleMapper.class)), docs, kbs);

    private void as(String role, Long dept) { UserContextHolder.set(new UserContext(1L,"test",dept,1L,role)); }
    @AfterEach void clear() { UserContextHolder.clear(); }

    @Test void employeeReadsPublicAndOwnDepartmentOnly() {
        as("EMPLOYEE", 1L);
        assertTrue(access.canRead(null,"PUBLIC"));
        assertTrue(access.canRead(1L,"DEPT"));
        assertFalse(access.canRead(2L,"PUBLIC"));
        assertFalse(access.canRead(2L,"DEPT"));
        assertFalse(access.canRead(1L,"CONFIDENTIAL"));
        assertFalse(access.canRead(null,"CONFIDENTIAL"));
        assertFalse(access.canRead(null,"DEPT"));
        assertThrows(BusinessException.class, () -> access.requireManage(1L));
    }
    @Test void departmentAdminCannotChangeAnotherDepartmentOrGlobalScope() {
        as("DEPT_ADMIN",1L);
        assertTrue(access.canRead(1L,"CONFIDENTIAL"));
        assertDoesNotThrow(() -> access.requireManage(1L));
        assertThrows(BusinessException.class, () -> access.requireManage(2L));
        assertThrows(BusinessException.class, () -> access.requireManage(null));
    }
    @Test void systemAdminCanManageAllScopes() {
        as("SYS_ADMIN",1L);
        assertTrue(access.canRead(2L,"CONFIDENTIAL"));
        assertDoesNotThrow(() -> access.requireManage(null));
        assertDoesNotThrow(() -> access.requireManage(2L));
    }
    @Test void missingOrUnknownIdentityFailsClosed() {
        assertFalse(access.canRead(null,"PUBLIC"));
        as("UNKNOWN",1L);
        assertFalse(access.canRead(null,"PUBLIC"));
        assertThrows(BusinessException.class, () -> access.requireManage(1L));
    }
    @Test void staleVectorCannotBypassCurrentDocumentOrKnowledgeBasePermissions() {
        as("EMPLOYEE",1L);
        KnowledgeBase kb = new KnowledgeBase(); kb.setId(5L); kb.setPermissionLevel("PUBLIC");
        KbDocument doc = new KbDocument(); doc.setId(10L); doc.setKbId(5L);
        doc.setPermissionLevel("PUBLIC"); doc.setStatus("READY");
        when(kbs.selectById(5L)).thenReturn(kb); when(docs.selectById(10L)).thenReturn(doc);
        assertTrue(access.canUseSource(10L));
        doc.setPermissionLevel("CONFIDENTIAL");
        assertFalse(access.canUseSource(10L));
        doc.setPermissionLevel("PUBLIC"); kb.setDepartmentId(2L);
        assertFalse(access.canUseSource(10L));
        kb.setDepartmentId(null); doc.setStatus("PARSING");
        assertFalse(access.canUseSource(10L));
        assertFalse(access.canUseSource(null));
        assertFalse(access.canUseSource(999L));
    }
}
