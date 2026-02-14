package com.example.highrps.shared;

import io.hypersistence.tsid.TSID;

public class IdGenerator {
    private IdGenerator() {}

    public static String generateString() {
        return TSID.Factory.getTsid().toString();
    }

    public static Long generateLong() {
        return TSID.Factory.getTsid().toLong();
    }
}
