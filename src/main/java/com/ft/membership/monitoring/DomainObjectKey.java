package com.ft.membership.monitoring;

public enum DomainObjectKey {
    UserId("userId"),
    UserEmail("userEmail"),
    ErightsId("erightsId"),
    ErightsGroupId("erightsGroupId");

    private final String key;

    DomainObjectKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
