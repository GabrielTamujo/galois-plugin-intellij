package com.galois.dto.request;

import org.jetbrains.annotations.NotNull;

public class PredictionRequestDto {

    @NotNull
    private final String text;

    public PredictionRequestDto(@NotNull String text) {
        this.text = text;
    }

}
