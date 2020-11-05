package com.galois.dto.request;

import org.jetbrains.annotations.NotNull;

public class PredictionAcceptedRequestDto {

    @NotNull
    private final String text;

    @NotNull
    private final String type;

    public PredictionAcceptedRequestDto(@NotNull String text, @NotNull String type) {
        this.text = text;
        this.type = type;
    }
}
