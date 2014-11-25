package com.ft.membership.monitoring;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

class Parameters {
    private Map<String, Object> params = new LinkedHashMap<>();

    protected void putWrapped(String key, Object value) {
        checkNotNull(key, "require key");
        if (value instanceof Number) {
            putNoWrap(key, value);
        } else {
            params.put(key, new ToStringWrapper(value));
        }
    }

    protected void putNoWrap(String key, Object value) {
        checkNotNull(key, "require key");
        params.put(key, value);
    }

    protected Map<String, Object> getParameters() {
        return params;
    }
}
