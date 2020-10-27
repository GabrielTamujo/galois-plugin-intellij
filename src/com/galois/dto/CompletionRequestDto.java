package com.galois.dto;

import org.jetbrains.annotations.NotNull;

public class CompletionRequestDto {

    @NotNull
    private String text;

    public CompletionRequestDto(@NotNull String text) {
        this.text = text;
    }

    @NotNull
    public String getText() {
        return text;
    }

}
