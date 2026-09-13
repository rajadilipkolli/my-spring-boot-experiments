package com.example.highrps.gatling.feeders;

import static io.gatling.javaapi.core.CoreDsl.*;

import com.example.highrps.gatling.config.LoadTestConfig;
import io.gatling.javaapi.core.FeederBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class PostFeeder {

    private static List<Map<String, Object>> postsCache = null;

    /**
     * Loads generated posts and their read weights once.
     *
     * @return cached post feeder rows
     */
    public static synchronized List<Map<String, Object>> loadPosts() {
        if (postsCache == null) {
            postsCache = new ArrayList<>();
            try {
                List<String> lines = Files.readAllLines(Paths.get(LoadTestConfig.DATA_DIR + "/posts.csv"));
                // skip header
                for (int i = 1; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    if (parts.length == 2) {
                        postsCache.add(Map.of("postId", parts[0], "weight", Integer.parseInt(parts[1])));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return postsCache;
    }

    /**
     * Creates a random feeder with uniform post selection.
     *
     * @return the post CSV feeder
     */
    public static FeederBuilder<String> getUniform() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/posts.csv")
                        .toAbsolutePath()
                        .toString())
                .random();
    }

    /**
     * Creates an infinite iterator that selects posts according to their weights.
     *
     * @return the weighted post iterator
     */
    public static Iterator<Map<String, Object>> getSkewed() {
        List<Map<String, Object>> posts = loadPosts();
        long totalWeight =
                posts.stream().mapToLong(p -> (Integer) p.get("weight")).sum();
        Random rand = new Random();

        return Stream.generate(() -> {
                    long target = (long) (rand.nextDouble() * totalWeight);
                    long current = 0;
                    for (Map<String, Object> p : posts) {
                        current += (Integer) p.get("weight");
                        if (current > target) {
                            return p;
                        }
                    }
                    return posts.getLast();
                })
                .iterator();
    }
}
