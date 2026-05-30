package com.fintech.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaqLoader implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        // 已有数据则跳过 —— 简单粗暴：查一次有没有结果
        List<Document> existing = vectorStore.similaritySearch(
                SearchRequest.builder().query("any").topK(1).build()
        );
        if (!existing.isEmpty()) {
            log.info("FAQ 知识库已有数据，跳过加载");
            return;
        }

        // 读 markdown
        Resource res = resourceLoader.getResource("classpath:faq/travel_faq.md");
        String md = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // 按 "## " 分段（每段一个文档），过滤空段
        List<Document> docs = Arrays.stream(md.split("(?m)^## "))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Document::new)
                .toList();

        vectorStore.add(docs);
        log.info("FAQ 知识库初始化完成，加载 {} 条", docs.size());

    }
}
