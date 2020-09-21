package com.galois;

import java.util.List;

public class CompletionResult {

    private List<String> result;

    public CompletionResult(List<String> result) {
        this.result = result;
    }

    public List<String> getResult() {
        return result;
    }

}
