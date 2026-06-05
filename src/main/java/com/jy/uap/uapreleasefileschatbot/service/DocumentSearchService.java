package com.jy.uap.uapreleasefileschatbot.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentSearchService {

    private static final int TOP_K = 10;

    private final VectorStore vectorStore;

    public DocumentSearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String searchReleaseDocuments(String query) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(TOP_K)
                .similarityThreshold(0.0)
                .build());

        if (documents.isEmpty()) {
            return "No matching UAP/UFO release document chunks were found.";
        }

        Set<String> sources = new LinkedHashSet<>();
        String chunks = documents.stream()
                .map(document -> formatChunk(document, sources))
                .collect(Collectors.joining("\n\n"));

        String sourceList = sources.isEmpty() ? "unknown" : String.join(", ", sources);
        return "Sources: " + sourceList + "\n\n" + chunks;
    }

    private String formatChunk(Document document, Set<String> sources) {
        String fileName = extractFileName(document.getMetadata());
        if (StringUtils.hasText(fileName)) {
            sources.add(fileName);
        }

        String header = StringUtils.hasText(fileName)
                ? "--- " + fileName + " ---"
                : "--- document chunk ---";
        return header + "\n" + document.getText();
    }

    private String extractFileName(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }

        Object fileName = metadata.get("fileName");
        if (fileName != null && StringUtils.hasText(String.valueOf(fileName))) {
            return String.valueOf(fileName);
        }

        Object source = metadata.get("source");
        if (source == null) {
            return null;
        }

        String sourceStr = String.valueOf(source);
        int slash = Math.max(sourceStr.lastIndexOf('/'), sourceStr.lastIndexOf('\\'));
        return slash >= 0 ? sourceStr.substring(slash + 1) : sourceStr;
    }

}
