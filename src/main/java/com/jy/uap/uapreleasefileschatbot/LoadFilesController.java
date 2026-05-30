package com.jy.uap.uapreleasefileschatbot;

import com.jy.uap.uapreleasefileschatbot.service.UapDocumentLoaderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class LoadFilesController {

    private final UapDocumentLoaderService documentLoaderService;

    public LoadFilesController(UapDocumentLoaderService documentLoaderService) {
        this.documentLoaderService = documentLoaderService;
    }

    @GetMapping("/loadFiles")
    public Map<String, Object> loadFiles() throws IOException {
        int chunksLoaded = documentLoaderService.loadDocuments();
        return Map.of(
                "message", chunksLoaded > 0
                        ? "Documents loaded and embedded successfully"
                        : "No documents found under resources/uapDocuments",
                "chunksLoaded", chunksLoaded
        );
    }
}
