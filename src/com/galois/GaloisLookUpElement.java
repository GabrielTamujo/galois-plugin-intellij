package com.galois;

public class GaloisLookUpElement {

    private final String type;
    private final String suffix;
    private final String insertText;

    public GaloisLookUpElement(String insertText, String type, String suffix) {
        this.insertText = insertText;
        this.type = type;
        this.suffix = suffix;
    }

    public String getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getInsertText() {
        return insertText;
    }
}
