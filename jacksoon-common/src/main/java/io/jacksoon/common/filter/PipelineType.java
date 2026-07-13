package io.jacksoon.common.filter;

public enum PipelineType {
    REQUEST_PARSE("parse"),
    ROUTING("router"),
    BACKEND_RESPONSE("backend-response");

    private final String event;

    PipelineType(String event) {
        this.event = event;
    }

    public String event() {
        return event;
    }

    public static PipelineType fromEvent(String event) {
        for (PipelineType type : values()) {
            if (type.event.equals(event)) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }
}
