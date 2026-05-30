package com.fintech.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgeTools {

    private final VectorStore vectorStore;

    @Tool(description = """
            在 FAQ 知识库中按用户的自然语言问题检索相关条目，返回 top-3 最相关的文档原文。
            使用时机：用户询问政策性、规则性、常识性问题——改签政策、退订规则、行李规定、入住流程、客服时间等。
            返回值：拼接好的文档原文字符串，按相关度排序。如果库中没找到，返回"知识库未命中"。
            注意：你拿到的就是原文，请基于这些原文如实回答用户问题，不要编造。
            """)
    public String searchKnowledge(
            @ToolParam(description = "用户的原始问题,用于语义检索。例：‘机票改签收手续费吗’或‘退订规则’或者‘酒店退房收手续费吗’")
            String query) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(3).build()
        );

        if (docs.isEmpty()) {
            return "知识库未能解答";
        }

        return docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
