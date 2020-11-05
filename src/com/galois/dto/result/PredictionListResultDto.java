package com.galois.dto.result;

import java.util.List;

public class PredictionListResultDto {

    private final List<PredictionResultDto> result;

    public PredictionListResultDto(List<PredictionResultDto> result) {
        this.result = result;
    }

    public List<PredictionResultDto> getResult() {
        return result;
    }

}
