package com.corpedia.ai;
import com.corpedia.config.RagProperties;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ChunkerTest {
    @Test void splitsChineseTextWithStableIdsAndPermissionMetadata() {
        RagProperties cfg = new RagProperties(); cfg.setChunkSize(10); cfg.setChunkOverlap(2);
        var chunks = new Chunker(cfg).split(8L, "企业内部知识库用于查询请假流程以及报销制度。", Map.of("department_id",2));
        assertEquals("doc-8-0", chunks.get(0).getId());
        assertEquals("doc-8-1", chunks.get(1).getId());
        assertEquals(2, chunks.get(1).getMetadata().get("department_id"));
        assertTrue(chunks.stream().allMatch(c -> c.getText().length() <= 10));
        assertEquals(chunks.get(0).getText().substring(8), chunks.get(1).getText().substring(0,2));
        assertTrue(chunks.get(chunks.size()-1).getText().endsWith("制度。"));
    }
    @Test void rejectsInvalidWindowAndHandlesEmptyInput() {
        RagProperties cfg = new RagProperties(); cfg.setChunkSize(0);
        assertThrows(IllegalArgumentException.class, () -> new Chunker(cfg).split(1L,"x",Map.of()));
        cfg.setChunkSize(100); cfg.setChunkOverlap(100);
        assertThrows(IllegalArgumentException.class, () -> new Chunker(cfg).split(1L,"x",Map.of()));
        cfg.setChunkOverlap(20);
        assertTrue(new Chunker(cfg).split(1L,"",Map.of()).isEmpty());
    }
}
