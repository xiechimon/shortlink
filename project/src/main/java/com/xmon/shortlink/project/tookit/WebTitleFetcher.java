package com.xmon.shortlink.project.tookit;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * 目标网站信息抓取工具（基于 Jsoup实现）
 */
@Slf4j
@Component
public class WebTitleFetcher {

    private static final int TIMEOUT_MS = 3000;

    /**
     * 获取目标 URL 的网页标题
     *
     * @param originUrl 目标链接
     * @return 网页标题；若无法获取则返回 null
     */
    public String fetchTitle(String originUrl) {
        if (originUrl == null || originUrl.isBlank()) {
            return null;
        }
        // Jsoup 需要绝对路径，如果用户没有带协议头，默认补齐 http://
        if (!originUrl.toLowerCase().startsWith("http://") && !originUrl.toLowerCase().startsWith("https://")) {
            originUrl = "http://" + originUrl;
        }

        try {
            // 使用 Jsoup 连接并限制最大获取体积为 2MB（Jsoup 默认值），超时时间 3秒
            Document document = Jsoup.connect(originUrl)
                    .timeout(TIMEOUT_MS)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    // 很多人反爬虫策略严格，需要补点常规的 Header
                    .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.9")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .get();

            // Jsoup 会自动解析 DOM 树，自动解码特殊 HTML 实体转义字符（如 &amp; -> &）
            String title = document.title();
            
            if (title != null && !title.isBlank()) {
                return title.trim();
            }
        } catch (Exception e) {
            log.warn("[WebTitleFetcher] 获取 {} 网页标题失败：{}", originUrl, e.getMessage());
        }
        return null;
    }
}
