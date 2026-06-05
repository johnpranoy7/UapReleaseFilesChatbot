package com.jy.uap.uapreleasefileschatbot.controller;

import com.jy.uap.uapreleasefileschatbot.dto.DocumentLoadResult;
import com.jy.uap.uapreleasefileschatbot.service.DocumentLoaderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class LoadFilesController {

    private final DocumentLoaderService documentLoaderService;

    public LoadFilesController(DocumentLoaderService documentLoaderService) {
        this.documentLoaderService = documentLoaderService;
    }

    @GetMapping("/loadFiles")
    public Map<String, Object> loadFiles() throws IOException {
        DocumentLoadResult result = documentLoaderService.loadDocuments();
        if (result.skipped()) {
            return Map.of(
                    "message", "Documents already indexed; skipping load",
                    "chunksLoaded", result.chunksLoaded(),
                    "skipped", true
            );
        }
        return Map.of(
                "message", result.chunksLoaded() > 0
                        ? "Documents loaded and embedded successfully"
                        : "No documents found under resources/uapDocuments",
                "chunksLoaded", result.chunksLoaded(),
                "skipped", false
        );
    }
}
