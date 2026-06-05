package com.jy.uap.uapreleasefileschatbot.dto;

public record DocumentLoadResult(int chunksLoaded, boolean skipped) {

    public static DocumentLoadResult loaded(int chunksLoaded) {
        return new DocumentLoadResult(chunksLoaded, false);
    }

    public static DocumentLoadResult skipped(int existingChunks) {
        return new DocumentLoadResult(existingChunks, true);
    }
}
