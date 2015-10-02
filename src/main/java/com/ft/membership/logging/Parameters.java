package com.ft.membership.logging;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

class Parameters {
    private Map<String, Object> params = new LinkedHashMap<>();

    protected void put(String key, Object value) {
        checkNotNull(key, "require key");
        params.put(key, value);
    }

    protected Map<String, Object> getParameters() {
        return params;
    }
}
