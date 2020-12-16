package com.galois.dto.request;

import org.jetbrains.annotations.NotNull;

public class PredictionRequestDto {

    @NotNull
    private final String text;

    private final Double topP;

    private final Integer topK;

    private final Double temperature;

    public PredictionRequestDto(@NotNull String text) {
        this.text = text;
        this.topP = 0.85;
        this.topK = 50;
        this.temperature = 0.5;
    }

}
