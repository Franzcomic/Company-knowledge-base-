package com.corpedia.ai;

import com.corpedia.config.RagProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 问答生成：检索 → 相似度阈值定答/拒答 → 拼 Prompt+Context → ChatClient 生成答案，附来源。
 * 结构化输出不做 JSON schema（本地 <7B 模型不可靠），改为「固定格式提示 + 直接返回正文」。
 */
@Service
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);

    /** 拒答兜底文案（与前端契约一致）。 */
    private static final String FALLBACK_REFUSE = "当前企业知识库中未找到足够可靠的信息，建议联系相关部门确认。";

    private static final String SYSTEM_PROMPT = """
            你是企业内部知识库智能助手。请严格依据提供的检索片段回答用户问题：
            1. 只依据检索片段中的信息作答，禁止编造、不得依据片段之外的内容。
            2. 答案需准确、简洁，可直接引用片段中的条款/数字/政策。
            3. 若片段确实无法回答该问题，请明确说明知识库中未找到相关信息。
            4. 全程使用中文。
            """;

    private final RagRetrieveService retrieveService;
    private final RagProperties rag;
    private final ChatClient chatClient;

    public RagChatService(RagRetrieveService retrieveService, RagProperties rag, ChatClient.Builder chatClientBuilder) {
        this.retrieveService = retrieveService;
        this.rag = rag;
        this.chatClient = chatClientBuilder.build();
    }

    /** 一次问答：检索 → 阈值定答 → 生成。sources 为用于取答的引用片段。 */
    public ChatResult chat(String question) {
        long start = System.currentTimeMillis();
        List<RetrievedChunk> hits = retrieveService.retrieve(question, rag.getTopK());

        // 阈值过滤：低于 similarity-threshold 视为无可靠依据
        double threshold = rag.getSimilarityThreshold();
        List<RetrievedChunk> relevant = hits.stream()
                .filter(h -> h.similarity() >= threshold)
                .toList();
        if (relevant.isEmpty()) {
            log.info("[RagChat] 拒答：无片段达到阈值 {} (共检索 {} 条，最高分 {})，耗时 {}ms",
                    threshold, hits.size(),
                    hits.isEmpty() ? "N/A" : String.format("%.4f", hits.get(0).similarity()),
                    System.currentTimeMillis() - start);
            return new ChatResult(FALLBACK_REFUSE, List.of(), false, hits.isEmpty() ? 0.0 : hits.get(0).similarity());
        }

        // 取相似度最高的前 rerankTop 条作为来源上下文
        List<RetrievedChunk> sources = relevant.subList(0, Math.min(relevant.size(), rag.getRerankTop()));
        double best = sources.get(0).similarity();

        String context = sources.stream()
                .map(c -> "[片段]\n" + c.content())
                .collect(Collectors.joining("\n\n"));

        String userPrompt = """
                问题：%s

                以下是企业知识库检索到的相关片段，请据此作答：
                %s
                """.formatted(question, context);

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();
        if (answer == null || answer.isBlank()) {
            answer = FALLBACK_REFUSE;
        }
        log.info("[RagChat] 定答（相似度 {}/{}，共引用 {} 条），耗时 {}ms",
                best, threshold, sources.size(), System.currentTimeMillis() - start);
        return new ChatResult(answer.strip(), sources, true, best);
    }
}