package com.galois.dto;

public class GaloisCompletionDto {

    private final String prediction;

    private final String type;

    private final String prefix;

    private final String suffix;

    public GaloisCompletionDto(String prediction, String type, String prefix, String suffix) {
        this.prediction = prediction;
        this.type = type;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getPrediction() {
        return prediction;
    }

    public String getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }
}
