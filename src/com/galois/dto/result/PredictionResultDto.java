package com.galois.dto.result;

public class PredictionResultDto {

    private final String prediction;

    private final String type;

    public PredictionResultDto(String prediction, String type) {
        this.prediction = prediction;
        this.type = type;
    }

    public String getPrediction() {
        return prediction;
    }

    public String getType() {
        return type;
    }
}
