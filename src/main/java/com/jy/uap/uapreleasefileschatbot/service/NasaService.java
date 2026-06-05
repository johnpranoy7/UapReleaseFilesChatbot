package com.jy.uap.uapreleasefileschatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class NasaService {

    private final RestClient restClient;
    private final String apiUrl;
    private final String apiKey;

    public NasaService(
            RestClient.Builder restClientBuilder,
            @Value("${nasa.api.url}") String apiUrl,
            @Value("${nasa.api-key}") String apiKey) {
        this.restClient = restClientBuilder.build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public ApodResult getNasaApod(String date) {
        log.info("Fetching NASA APOD for date: {}", date);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("api_key", apiKey);

        if (StringUtils.hasText(date)) {
            uriBuilder.queryParam("date", date.trim());
        }

        ApodResult response;
        try {
            response = restClient.get()
                    .uri(uriBuilder.build().toUri())
                    .retrieve()
                    .body(ApodResult.class);
        } catch (Exception ex) {
            log.error("NASA APOD API call failed for date {}", date, ex);
            throw new IllegalStateException("Unable to reach NASA APOD API", ex);
        }

        if (response == null) {
            log.error("NASA APOD API returned an empty response for date {}", date);
            throw new IllegalStateException("NASA APOD API returned an empty response");
        }

        log.info("Fetched APOD: {} ({})", response.title(), response.date());
        return response;
    }

    public record ApodResult(
            String title,
            String date,
            String explanation,
            String url
    ) {

        public String toToolResponseText() {
            StringBuilder builder = new StringBuilder();
            builder.append("Title: ").append(title).append('\n');
            builder.append("Date: ").append(date).append('\n');

            if (StringUtils.hasText(url)) {
                builder.append("Image URL: ").append(url).append('\n');
            }

            builder.append("Explanation: ").append(truncate(explanation));
            return builder.toString();
        }

        private static String truncate(String text) {
            if (text == null || text.length() <= 600) {
                return text;
            }
            return text.substring(0, 600) + "...";
        }

    }

}
