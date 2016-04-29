package com.ft.membership.logging;

public enum DomainObjectKey {
    UserId("userId"),
    UserEmail("email");

    private final String key;

    DomainObjectKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
