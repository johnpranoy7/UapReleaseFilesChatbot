package com.jy.uap.uapreleasefileschatbot.config;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.jy.uap.uapreleasefileschatbot.service.DocumentSearchService;
import com.jy.uap.uapreleasefileschatbot.service.NasaService;
import com.jy.uap.uapreleasefileschatbot.service.NasaService.ApodResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppTools {

    private final NasaService nasaService;
    private final DocumentSearchService documentSearchService;

    public AppTools(NasaService nasaService, DocumentSearchService documentSearchService) {
        this.nasaService = nasaService;
        this.documentSearchService = documentSearchService;
    }

    @Tool(description = """
            Search UAP/UFO release document files for information relevant to the user's question.
            Use for questions about release files, sightings, intelligence reports, or document content.
            Pass the user's question as the query.
            """)
    public String searchUapReleaseDocuments(
            @ToolParam(description = "The user's question or search query about UAP/UFO release documents")
            String query) {

        log.info("searchUapReleaseDocuments called with query: {}", query);
        return documentSearchService.searchReleaseDocuments(query);
    }

    @Tool(description = """
            Fetch NASA's Astronomy Picture of the Day (APOD).
            Use when the user asks for today's APOD, a NASA astronomy image, or APOD for a specific date.
            Resolve relative dates using today's date from the system prompt.
            Call this tool at most once per user request.
            """)
    public String getNasaApod(
            @ToolParam(
                    description = "Resolved date in YYYY-MM-DD. Optional",
                    required = false)
            @Nullable String date) {

        log.info("getNasaApod called with date: {}", date);
        String requestedDate = StringUtils.hasText(date) ? date.trim() : null;
        ApodResult result = nasaService.getNasaApod(requestedDate);
        return result.toToolResponseText();
    }

}
