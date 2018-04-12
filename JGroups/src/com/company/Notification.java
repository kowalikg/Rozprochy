package com.company;

import java.io.Serializable;

public class Notification implements Serializable {
    private final Operation operation;
    private final String key;
    private final String value;

    public Notification(Operation operation, String key, String value) {
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    public Notification(Operation operation, String key) {
        this.operation = operation;
        this.key = key;
        this.value = null;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
