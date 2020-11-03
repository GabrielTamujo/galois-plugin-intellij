package com.galois.dto;

import org.jetbrains.annotations.NotNull;

public class PredictionAcceptedRequestDto {

    @NotNull
    private final String text;

    private final String type;

    public PredictionAcceptedRequestDto(@NotNull String text, String type) {
        this.text = text;
        this.type = type;
    }

    @NotNull
    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
}
