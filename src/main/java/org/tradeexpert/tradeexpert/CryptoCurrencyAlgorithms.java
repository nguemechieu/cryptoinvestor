package org.tradeexpert.tradeexpert;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public final class CryptoCurrencyAlgorithms {
    private static final Map<String, Algorithm> ALGORITHM_MAP = new ConcurrentHashMap<>();

    static {
        for (Algorithm algorithm : Algorithm.values()) ALGORITHM_MAP.put(algorithm.toString(), algorithm);
    }

    private CryptoCurrencyAlgorithms() {
        throw new AssertionError("No instances.");
    }

    public static Algorithm getAlgorithm(String algorithm) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        if (algorithm.equals("?")) {
            return Algorithm.UNKNOWN;
        }

        return ALGORITHM_MAP.get(algorithm) != null ? ALGORITHM_MAP.get(algorithm) : Algorithm.NULL;
    }
}
