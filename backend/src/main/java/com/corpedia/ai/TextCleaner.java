package com.corpedia.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 文档文本清洗：去 BOM/页眉页脚样式噪音、分隔线、重复段、多余空行。
 */
@Component
public class TextCleaner {

    private static final Pattern SEPARATOR = Pattern.compile("^(?:[-*═=~_]{3,}|[#]{1,6}\\s*)$");

    public String clean(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        StringBuilder sb = new StringBuilder(text.length());
        String prev = null;
        int blanks = 0;
        for (String line : text.split("\n")) {
            String s = line.strip();
            if (s.isEmpty()) {
                blanks++;
                if (blanks <= 1) {
                    sb.append('\n');
                }
                continue;
            }
            blanks = 0;
            if (SEPARATOR.matcher(s).matches()) {
                continue; // 分隔线
            }
            if (s.equals(prev)) {
                continue; // 重复段
            }
            sb.append(s).append('\n');
            prev = s;
        }
        return sb.toString().strip();
    }
}
