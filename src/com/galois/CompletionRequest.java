package com.galois;

import org.jetbrains.annotations.NotNull;

public class CompletionRequest {

    @NotNull
    private String text;

    public CompletionRequest(@NotNull String text) {
        this.text = text;
    }

    @NotNull
    public String getText() {
        return text;
    }

}
