package com.example.highrps.gatling.feeders;

import static io.gatling.javaapi.core.CoreDsl.*;

import com.example.highrps.gatling.config.LoadTestConfig;
import io.gatling.javaapi.core.FeederBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TagFeeder {

    private static Map<String, List<String>> tagToPosts = null;

    /**
     * Loads the generated tag-to-post index once.
     */
    public static synchronized void loadTagToPosts() {
        if (tagToPosts == null) {
            tagToPosts = new HashMap<>();
            try {
                List<String> lines = Files.readAllLines(Paths.get(LoadTestConfig.DATA_DIR + "/post_tags.csv"));
                // skip header
                for (int i = 1; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    if (parts.length == 2) {
                        tagToPosts
                                .computeIfAbsent(parts[0], k -> new ArrayList<>())
                                .add(parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a random feeder over generated tags.
     *
     * @return the tag CSV feeder
     */
    public static FeederBuilder<String> getTags() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/tags.csv")
                        .toAbsolutePath()
                        .toString())
                .random();
    }

    /**
     * Returns generated post identifiers associated with a tag.
     *
     * @param tag the tag name
     * @return associated post identifiers
     */
    public static List<String> getPostsForTag(String tag) {
        if (tagToPosts == null) {
            loadTagToPosts();
        }
        return tagToPosts.getOrDefault(tag, Collections.emptyList());
    }
}
