package com.restfulbooker.api.tests.base;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe key/value store for sharing data between chained test steps in E2E flows.
 * Each thread gets its own isolated map.
 */
public final class ScenarioContext {

    private static final ThreadLocal<ConcurrentHashMap<String, Object>> context =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    private ScenarioContext() {}

    /** Stores a value under the given key for the current thread. */
    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    /** Retrieves and casts the value for the current thread. */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) context.get().get(key);
    }

    /** Clears the context for the current thread. */
    public static void clear() {
        context.get().clear();
    }
}
