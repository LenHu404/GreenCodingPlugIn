package com.example.plugintest.settings;


public enum Mode {
    FEW_SHOT("few-shot"),
    ONE_SHOT("one-shot");

    private final String value;

    Mode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Mode fromString(String value) {
        for (Mode mode : Mode.values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return null;
    }
}

