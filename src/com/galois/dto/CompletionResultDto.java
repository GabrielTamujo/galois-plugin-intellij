package com.galois.dto;

import java.util.List;

public class CompletionResultDto {

    private List<String> result;

    public CompletionResultDto(List<String> result) {
        this.result = result;
    }

    public List<String> getResult() {
        return result;
    }

}
