package com.ft.membership.logging;

import com.ft.membership.common.types.userid.UserId;

public class Yield extends Parameters implements LoggingTerminal {
    private final Operation operation;

    public Yield(final Operation operation) {
        this.operation = operation;
    }

    public Yield yielding(final UserId userId) {
        put("userId", userId);
        return this;
    }

    public Yield yielding(final String key, final Object value) {
        put(key, value);
        return this;
    }

    public Yield yielding(final DomainObjectKey key, final Object value) {
        put(key.getKey(), value);
        return this;
    }

    public void log() {
        log(operation.getActorOrLogger());
    }

    public void log(final Object actorOrLogger) {
        logInfo(actorOrLogger);
    }

    public void logInfo(Object actorOrLogger) {
        new LogFormatter(actorOrLogger).logInfo(operation, this);
    }

}
