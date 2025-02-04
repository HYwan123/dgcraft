package com.example.wan_try.dglab;

public enum DgLabPackType {
    Bind("bind"),
    Break("break"),
    Msg("msg"),
    Heartbeat("heartbeat"),
    Error("error");

    private final String value;

    DgLabPackType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
