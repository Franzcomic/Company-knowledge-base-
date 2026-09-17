package com.corpedia.service;

import com.corpedia.dto.response.DepartmentStatVO;
import com.corpedia.dto.response.HotKbVO;
import com.corpedia.dto.response.HotQuestionVO;
import com.corpedia.dto.response.StatsOverviewVO;
import com.corpedia.dto.response.TrendPointVO;
import com.corpedia.entity.KbDocument;
import com.corpedia.entity.KnowledgeBase;
import com.corpedia.mapper.KbDocumentMapper;
import com.corpedia.mapper.KnowledgeBaseMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 阶段5 统计聚合（模块⑧ P2）：基于 message/feedback 实时聚合，
 * 提供趋势/热门问题/热门知识库/部门分布/命中率/响应时间/满意率。
 * 当前不做 qa_statistics 定时落表与 Redis 缓存（数据量与实时聚合场景暂不需要），
 * 如后续量大再引入。
 */
@Service
public class StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final JdbcTemplate jdbc;
    private final KbDocumentMapper documentMapper;
    private final KnowledgeBaseMapper kbMapper;
    private final ObjectMapper objectMapper;

    public StatsService(JdbcTemplate jdbc, KbDocumentMapper documentMapper,
                        KnowledgeBaseMapper kbMapper, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.documentMapper = documentMapper;
        this.kbMapper = kbMapper;
        this.objectMapper = objectMapper;
    }

    /** 总览：总量/命中率/响应时间/满意率/逐日趋势。 */
    public StatsOverviewVO overview(String range) {
        LocalDateTime from = parseRange(range);

        long total = count("select count(*) from message where role = 'USER' and created_at >= ?", from);
        long answered = count("select count(*) from message where role = 'ASSISTANT' and answered = 1 and created_at >= ?", from);
        long notAnswered = count("select count(*) from message where role = 'ASSISTANT' and answered = 0 and created_at >= ?", from);

        Double avgResponseMs = queryDouble(
                "select avg(response_ms) from message where role = 'ASSISTANT' and response_ms is not null and created_at >= ?",
                from);

        Double satisfaction = satisfaction(from);

        Double hitRate = total > 0 ? (double) answered / total : 0.0;

        List<TrendPointVO> trend = trend(from);

        return new StatsOverviewVO(total, answered, notAnswered, hitRate, avgResponseMs, satisfaction, trend);
    }

    /** 热门问题：按问题原文分组计数，取 Top N。 */
    public List<HotQuestionVO> hotQuestions(String range, int limit) {
        LocalDateTime from = parseRange(range);
        return jdbc.query(
                "select content, count(*) as c from message where role = 'USER' and created_at >= ? " +
                        "group by content order by c desc, max(created_at) desc limit " + limit,
                (rs, i) -> new HotQuestionVO(rs.getString("content"), rs.getLong("c")),
                from);
    }

    /** 热门知识库：解析 assistant 来源引用，统计各知识库被引次数。 */
    public List<HotKbVO> hotKb(String range, int limit) {
        LocalDateTime from = parseRange(range);
        // 取范围内的所有来源 JSON
        List<String> sources = jdbc.query(
                "select sources from message where role = 'ASSISTANT' and sources is not null and created_at >= ?",
                (rs, i) -> rs.getString("sources"), from);

        // 统计 documentId -> 引用次数
        Map<Long, Long> refCount = new LinkedHashMap<>();
        for (String json : sources) {
            try {
                List<Map<String, Object>> arr = objectMapper.readValue(json, new TypeReference<>() {});
                for (Map<String, Object> s : arr) {
                    Object id = s.get("documentId");
                    if (id instanceof Number n) {
                        refCount.merge(n.longValue(), 1L, Long::sum);
                    }
                }
            } catch (Exception e) {
                log.warn("[Stats] 来源 JSON 解析失败: {}", e.getMessage());
            }
        }
        if (refCount.isEmpty()) {
            return List.of();
        }

        // documentId -> 知识库
        List<Long> docIds = new ArrayList<>(refCount.keySet());
        Map<Long, String> docKbName = new LinkedHashMap<>();
        docIds.stream()
                .map(documentMapper::selectById)
                .filter(Objects::nonNull)
                .forEach(doc -> {
                    KnowledgeBase kb = doc.getKbId() == null ? null : kbMapper.selectById(doc.getKbId());
                    docKbName.put(doc.getId(), kb == null ? "未知知识库" : kb.getName());
                });

        // 按参数顺序聚合（越靠前引用越多）
        Map<String, Long> kbCount = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> e : refCount.entrySet()) {
            kbCount.merge(docKbName.getOrDefault(e.getKey(), "未知知识库"), e.getValue(), Long::sum);
        }
        return kbCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new HotKbVO(e.getKey(), e.getValue()))
                .toList();
    }

    /** 部门分布：按会话发起用户的部门统计问题数。 */
    public List<DepartmentStatVO> departments(String range) {
        LocalDateTime from = parseRange(range);
        return jdbc.query(
                "select coalesce(d.name, '未分配') as deptName, count(*) as c " +
                        "from message m join conversation cv on m.conversation_id = cv.id " +
                        "left join department d on cv.department_id = d.id " +
                        "where m.role = 'USER' and m.created_at >= ? " +
                        "group by cv.department_id, d.name order by c desc",
                (rs, i) -> new DepartmentStatVO(rs.getString("deptName"), rs.getLong("c")),
                from);
    }

    /** 未解决问题：返回被拒答（answered=0）的问题原文列表。 */
    public List<String> unsolved(String range, int limit) {
        LocalDateTime from = parseRange(range);
        List<Long> refusedAssistantIds = jdbc.query(
                "select id, conversation_id from message where role = 'ASSISTANT' and answered = 0 and created_at >= ? " +
                        "order by id desc",
                (rs, i) -> rs.getLong("id"), from);

        if (refusedAssistantIds.isEmpty()) {
            return List.of();
        }
        // 各拒答 assistant 前一条同会话 USER 消息即问题原文
        List<String> result = new ArrayList<>();
        for (Long assistantId : refusedAssistantIds) {
            List<String> qs = jdbc.query(
                    "select content from message where role = 'USER' and conversation_id = " +
                            "(select conversation_id from message where id = ?) and id < ? order by id desc limit 1",
                    (rs, i) -> rs.getString("content"), assistantId, assistantId);
            String question = qs.isEmpty() ? null : qs.get(0);
            if (question != null && !question.isBlank()) {
                result.add(question);
            }
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    /** 满意率：反馈 UP / (UP+DOWN)，无反馈返回 null。 */
    private Double satisfaction(LocalDateTime from) {
        List<Long> ups = jdbc.query(
                "select count(*) from feedback f join message m on f.message_id = m.id " +
                        "where f.rating = 'UP' and m.created_at >= ?",
                (rs, i) -> rs.getLong(1), from);
        List<Long> downs = jdbc.query(
                "select count(*) from feedback f join message m on f.message_id = m.id " +
                        "where f.rating = 'DOWN' and m.created_at >= ?",
                (rs, i) -> rs.getLong(1), from);
        long up = ups.isEmpty() ? 0 : ups.get(0);
        long down = downs.isEmpty() ? 0 : downs.get(0);
        long total = up + down;
        if (total == 0) {
            return null;
        }
        return (double) up / total;
    }

    /** 逐日趋势：从起始日到今天逐日补全（缺日为 0）。 */
    private List<TrendPointVO> trend(LocalDateTime from) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select date(created_at) as d, count(*) as c from message " +
                        "where role = 'USER' and created_at >= ? group by date(created_at) order by d",
                from);
        Map<LocalDate, Long> byDate = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            java.sql.Date d = (java.sql.Date) r.get("d");
            byDate.put(d.toLocalDate(), ((Number) r.get("c")).longValue());
        }
        List<TrendPointVO> trend = new ArrayList<>();
        LocalDate start = from.toLocalDate();
        LocalDate today = LocalDate.now();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            trend.add(new TrendPointVO(date, byDate.getOrDefault(date, 0L)));
        }
        return trend;
    }

    /** 解析 range 参数（7d/30d/90d/180d/365d/all），默认最近 7 天。 */
    private LocalDateTime parseRange(String range) {
        if (range == null || range.isBlank()) {
            range = "7d";
        }
        if ("all".equalsIgnoreCase(range) || "0".equals(range)) {
            return LocalDateTime.of(2000, 1, 1, 0, 0);
        }
        if (range.endsWith("d")) {
            try {
                int days = Integer.parseInt(range.substring(0, range.length() - 1));
                return LocalDateTime.now().minusDays(days);
            } catch (NumberFormatException ignored) {
                // fallthrough -> 默认
            }
        }
        // 也可支持 ISO 日期端点（可选）
        try {
            return LocalDateTime.parse(range);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.now().minusDays(7);
        }
    }

    private long count(String sql, LocalDateTime from) {
        List<Long> r = jdbc.query(sql, (rs, i) -> rs.getLong(1), from);
        return r.isEmpty() ? 0 : r.get(0);
    }

    private Double queryDouble(String sql, LocalDateTime from) {
        List<Double> r = jdbc.query(sql, (rs, i) -> {
            double v = rs.getDouble(1);
            return rs.wasNull() ? null : v;
        }, from);
        return r.isEmpty() ? null : r.get(0);
    }
}