package com.example.plugintest;

public class AiAnswer {
    String codeOutput = "";
    String reason = "";
    int[] lines = new int[0];

    public AiAnswer(String codeOutput, String reason, int[] lines) {
        this.reason = reason;
        this.codeOutput = codeOutput;
        this.lines = lines;
    }
}
