package com.example.highrps.gatling.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class LoadTestConfig {

    private static final Properties FILE_PROPERTIES = loadProperties();

    // Profiles: smoke, normal, high, stress
    public static final String PROFILE = getProperty("profile", "smoke");

    public static final String BASE_URL = getProperty("baseUrl", "http://localhost:8080");

    // Dataset sizes
    public static final int AUTHORS_SIZE = getIntProperty("authors", 100);
    public static final int POSTS_SIZE = getIntProperty("posts", 1000);
    public static final int COMMENTS_SIZE = getIntProperty("comments", 5000);
    public static final int TAGS_SIZE = getIntProperty("tags", 50);

    public static final int MUTABLE_POST_POOL_SIZE = getIntProperty("mutablePostPoolSize", 50);
    public static final int DELETABLE_POST_POOL_SIZE = getProfileDeletablePostPoolSize();
    public static final int MUTABLE_COMMENT_POOL_SIZE = getIntProperty("mutableCommentPoolSize", 50);
    public static final int DELETABLE_COMMENT_POOL_SIZE = getProfileDeletableCommentPoolSize();

    // Traffic weights
    public static final double READ_POST_WEIGHT = getDoubleProperty("readPostWeight", 41.0);
    public static final double READ_COMMENTS_WEIGHT = getDoubleProperty("readCommentsWeight", 20.0);
    public static final double READ_TAG_POSTS_WEIGHT = getDoubleProperty("readTagPostsWeight", 10.0);
    public static final double CREATE_COMMENT_WEIGHT = getDoubleProperty("createCommentWeight", 15.0);
    public static final double CREATE_POST_WEIGHT = getDoubleProperty("createPostWeight", 8.0);
    public static final double REGISTER_AUTHOR_WEIGHT = getDoubleProperty("registerAuthorWeight", 2.0);

    public static final double UPDATE_POST_WEIGHT = getDoubleProperty("postUpdateWeight", 1.0);
    public static final double DELETE_POST_WEIGHT = getDoubleProperty("postDeleteWeight", 1.0);
    public static final double UPDATE_COMMENT_WEIGHT = getDoubleProperty("commentUpdateWeight", 1.0);
    public static final double DELETE_COMMENT_WEIGHT = getDoubleProperty("commentDeleteWeight", 1.0);

    // Global properties that can be overridden by profile
    public static final double TARGET_RPS = getProfileTargetRps();
    public static final int DURATION_MINUTES = getProfileDurationMinutes();
    public static final int WARMUP_MINUTES = getProfileWarmupMinutes();

    // Assertion thresholds
    public static final double MAX_ERROR_RATE = getDoubleProperty("maxErrorRate", 1.0); // 1%
    public static final int BASELINE_P95_MS = getIntProperty("baseline.p95", 50);
    public static final int BASELINE_P99_MS = getIntProperty("baseline.p99", 100);
    public static final double BASELINE_THROUGHPUT = getDoubleProperty("baseline.throughput", TARGET_RPS * 0.9);

    // Allowed deltas
    public static final int ALLOWED_P95_DELTA_PERCENT = getIntProperty("allowed.p95.delta.percent", 5);
    public static final int ALLOWED_P99_DELTA_PERCENT = getIntProperty("allowed.p99.delta.percent", 10);
    public static final int ALLOWED_THROUGHPUT_DELTA_PERCENT = getIntProperty("allowed.throughput.delta.percent", 10);

    public static final String DATA_DIR = getProperty("dataDir", "target/loadtest-data");
    public static final long DATA_SEED = getLongProperty("dataSeed", 2674L);

    /**
     * Resolves a setting from a system property, environment variable, or default value.
     *
     * @param key the property name
     * @param defaultValue the fallback value
     * @return the resolved value
     */
    private static String getProperty(String key, String defaultValue) {
        return Optional.ofNullable(System.getProperty(key))
                .orElseGet(() -> Optional.ofNullable(
                                System.getenv(key.toUpperCase().replace('.', '_')))
                        .orElseGet(() -> FILE_PROPERTIES.getProperty(key, defaultValue)));
    }

    /**
     * Resolves an integer setting.
     *
     * @param key the property name
     * @param defaultValue the fallback value
     * @return the resolved integer
     */
    private static int getIntProperty(String key, int defaultValue) {
        String val = getProperty(key, null);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    /**
     * Resolves a decimal setting.
     *
     * @param key the property name
     * @param defaultValue the fallback value
     * @return the resolved decimal
     */
    private static double getDoubleProperty(String key, double defaultValue) {
        String val = getProperty(key, null);
        return val != null ? Double.parseDouble(val) : defaultValue;
    }

    /**
     * Resolves a long-valued setting.
     *
     * @param key the property name
     * @param defaultValue the fallback value
     * @return the resolved long value
     */
    private static long getLongProperty(String key, long defaultValue) {
        String val = getProperty(key, null);
        return val != null ? Long.parseLong(val) : defaultValue;
    }

    /**
     * Loads the optional load-test property file from the classpath.
     *
     * @return the loaded properties, or an empty set when the resource is absent
     * @throws IllegalStateException when the resource cannot be read
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = LoadTestConfig.class.getClassLoader().getResourceAsStream("load-test.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load load-test.properties", exception);
        }
        return properties;
    }

    /**
     * Resolves the target request rate for the selected profile.
     *
     * @return requests per second
     */
    private static double getProfileTargetRps() {
        String override = getProperty("targetRps", null);
        if (override != null) {
            return Double.parseDouble(override);
        }
        return switch (PROFILE) {
            case "smoke" -> 10.0;
            case "normal" -> 50.0;
            case "high", "stress" -> 100.0;
            default -> 10.0;
        };
    }

    /**
     * Resolves the steady-state duration for the selected profile.
     *
     * @return duration in minutes
     */
    private static int getProfileDurationMinutes() {
        String override = getProperty("durationMinutes", null);
        if (override != null) {
            return Integer.parseInt(override);
        }
        return switch (PROFILE) {
            case "smoke" -> 5;
            case "normal", "high", "stress" -> 15;
            default -> 5;
        };
    }

    /**
     * Resolves the warm-up duration for the selected profile.
     *
     * @return warm-up time in minutes
     */
    private static int getProfileWarmupMinutes() {
        String override = getProperty("warmupMinutes", null);
        if (override != null) {
            return Integer.parseInt(override);
        }
        return switch (PROFILE) {
            case "smoke" -> 1;
            case "normal", "high", "stress" -> 3;
            default -> 5;
        };
    }

    /**
     * Resolves the number of posts reserved for destructive scenarios.
     *
     * @return the deletable post pool size for the selected profile
     */
    private static int getProfileDeletablePostPoolSize() {
        String override = getProperty("deletablePostPoolSize", null);
        if (override != null) {
            return Integer.parseInt(override);
        }
        return switch (PROFILE) {
            case "smoke" -> 100;
            case "normal" -> 1000;
            case "high" -> 20000;
            case "stress" -> 25000;
            default -> 1000;
        };
    }

    /**
     * Resolves the number of comments reserved for destructive scenarios.
     *
     * @return the deletable comment pool size for the selected profile
     */
    private static int getProfileDeletableCommentPoolSize() {
        String override = getProperty("deletableCommentPoolSize", null);
        if (override != null) {
            return Integer.parseInt(override);
        }
        return switch (PROFILE) {
            case "smoke" -> 500;
            case "normal" -> 5000;
            case "high", "stress" -> 50000;
            default -> 5000;
        };
    }
}
