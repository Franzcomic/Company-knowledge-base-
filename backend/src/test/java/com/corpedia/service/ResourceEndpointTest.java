package com.corpedia.service;

import com.corpedia.ai.DocumentPipelineService;
import com.corpedia.common.BusinessException;
import com.corpedia.config.*;
import com.corpedia.dto.request.KbCreateRequest;
import com.corpedia.entity.*;
import com.corpedia.mapper.*;
import com.corpedia.security.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceEndpointTest {
    private final KnowledgeBaseMapper kbMapper=mock(KnowledgeBaseMapper.class);
    private final KbDocumentMapper docMapper=mock(KbDocumentMapper.class);
    private final DocumentPipelineService pipeline=mock(DocumentPipelineService.class);
    private final MilvusSchemaInitializer milvus=mock(MilvusSchemaInitializer.class);
    private final ResourceAccessService access=new ResourceAccessService(
            new PermissionService(mock(UserMapper.class),mock(RoleMapper.class)),docMapper,kbMapper);
    private final KbService kbs=new KbService(kbMapper,docMapper,mock(DocumentService.class),access);
    private final DocumentService docs=new DocumentService(docMapper,kbs,pipeline,milvus,new StorageProperties(),access);
    private KnowledgeBase kb;
    private KbDocument doc;
    @BeforeEach void setUp() {
        UserContextHolder.set(new UserContext(1L,"test",1L,3L,"EMPLOYEE"));
        kb=new KnowledgeBase();kb.setId(5L);kb.setDepartmentId(1L);kb.setPermissionLevel("DEPT");
        doc=new KbDocument();doc.setId(10L);doc.setKbId(5L);doc.setDepartmentId(1L);doc.setPermissionLevel("DEPT");doc.setStatus("READY");
        when(kbMapper.selectById(5L)).thenReturn(kb);when(docMapper.selectById(10L)).thenReturn(doc);
    }
    @AfterEach void clear() {UserContextHolder.clear();}
    @Test void employeeCannotCreateOrUploadEvenInOwnDepartment() {
        assertThrows(BusinessException.class,()->kbs.create(new KbCreateRequest("test",1L,"DEPT",null),1L));
        assertThrows(BusinessException.class,()->docs.upload(5L,new MockMultipartFile("file","doc.md","text/plain","hello".getBytes()),1L));
        verify(kbMapper,never()).insert(any(KnowledgeBase.class));
        verifyNoInteractions(pipeline,milvus);
    }
    @Test void crossDepartmentDetailsAndChunksCannotBypassListFiltering() {
        kb.setDepartmentId(2L);doc.setDepartmentId(2L);
        assertThrows(BusinessException.class,()->docs.getDetail(10L));
        assertThrows(BusinessException.class,()->docs.listChunks(10L));
        assertThrows(BusinessException.class,()->docs.delete(10L));
        verifyNoInteractions(milvus,pipeline);
    }
    @Test void knowledgeBaseListOnlyContainsReadableResources() {
        var other=new KnowledgeBase();other.setId(6L);other.setDepartmentId(2L);other.setPermissionLevel("PUBLIC");
        when(kbMapper.selectList(any())).thenReturn(List.of(kb,other));
        assertEquals(List.of(5L),kbs.list().stream().map(v->v.id()).toList());
    }
    @Test void pathTraversalFilenameIsRejectedBeforeWritingOrIndexing() {
        UserContextHolder.set(new UserContext(1L,"admin",1L,1L,"SYS_ADMIN"));
        for(String name:List.of("../escape.md","folder/escape.md","..\\escape.md","C:escape.md")) {
            assertThrows(BusinessException.class,()->docs.upload(5L,new MockMultipartFile("file",name,"text/plain","hello".getBytes()),1L));
        }
        verify(docMapper,never()).insert(any(KbDocument.class));verifyNoInteractions(pipeline,milvus);
    }
    @Test void deletingAnInFlightDocumentCannotRaceItsPipeline() {
        UserContextHolder.set(new UserContext(1L,"admin",1L,1L,"SYS_ADMIN"));doc.setStatus("PARSING");
        assertThrows(BusinessException.class,()->docs.delete(10L));verifyNoInteractions(milvus);
    }
}
