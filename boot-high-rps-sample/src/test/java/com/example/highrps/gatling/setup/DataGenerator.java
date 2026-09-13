package com.example.highrps.gatling.setup;

import com.example.highrps.gatling.config.LoadTestConfig;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DataGenerator {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Random random = new Random();

    /**
     * Generates load-test fixtures through the running sample API.
     *
     * @param args ignored command-line arguments
     * @throws Exception when fixture creation or file output fails
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Data Generation...");

        Path dataDir = Paths.get(LoadTestConfig.DATA_DIR);
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        List<String> authors = generateAuthors(LoadTestConfig.AUTHORS_SIZE);
        List<String> tags = generateTags(LoadTestConfig.TAGS_SIZE);
        List<String> posts = generatePosts(LoadTestConfig.POSTS_SIZE, authors, tags);
        generateComments(LoadTestConfig.COMMENTS_SIZE, posts, authors);

        System.out.println("Data Generation Completed!");
    }

    /**
     * Creates authors and writes successful identities to the author feeder.
     *
     * @param count the number of authors to request
     * @return successfully created author emails
     * @throws Exception when an HTTP request or file operation fails
     */
    private static List<String> generateAuthors(int count) throws Exception {
        System.out.println("Generating " + count + " authors...");
        List<String> authors = new ArrayList<>();
        try (PrintWriter writer = new PrintWriter(new FileWriter(LoadTestConfig.DATA_DIR + "/authors.csv"))) {
            writer.println("email");
            for (int i = 0; i < count; i++) {
                String email = "author" + i + "_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
                String json = "{\"firstName\":\"Author" + i
                        + "\", \"lastName\":\"Test\", \"mobile\":\"1234567890\", \"email\":\"" + email + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(LoadTestConfig.BASE_URL + "/api/author"))
                        .header("Content-Type", "application/json")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 201) {
                    authors.add(email);
                    writer.println(email);
                } else {
                    System.err.println("Failed to create author: " + response.statusCode() + " " + response.body());
                }
            }
        }
        return authors;
    }

    /**
     * Generates tag names and their feeder file.
     *
     * @param count the number of tags to generate
     * @return generated tag names
     * @throws Exception when the feeder file cannot be written
     */
    private static List<String> generateTags(int count) throws Exception {
        System.out.println("Generating " + count + " tags...");
        List<String> tags = new ArrayList<>();
        try (PrintWriter writer = new PrintWriter(new FileWriter(LoadTestConfig.DATA_DIR + "/tags.csv"))) {
            writer.println("tag");
            for (int i = 0; i < count; i++) {
                String tag = "tag" + i;
                tags.add(tag);
                writer.println(tag);
            }
        }
        return tags;
    }

    /**
     * Creates posts and writes post and tag-association feeders.
     *
     * @param count the number of posts to request
     * @param authors available author emails
     * @param tags available tag names
     * @return successfully created post identifiers
     * @throws Exception when an HTTP request or file operation fails
     */
    private static List<String> generatePosts(int count, List<String> authors, List<String> tags) throws Exception {
        System.out.println("Generating " + count + " posts...");
        List<String> posts = new ArrayList<>();

        try (PrintWriter writer = new PrintWriter(new FileWriter(LoadTestConfig.DATA_DIR + "/posts.csv"));
                PrintWriter tagWriter = new PrintWriter(new FileWriter(LoadTestConfig.DATA_DIR + "/post_tags.csv"))) {
            writer.println("postId,weight");
            tagWriter.println("tag,postId");

            for (int i = 0; i < count; i++) {
                // Skewed author distribution
                String author = authors.get(getSkewedIndex(authors.size()));

                // Select 2-5 random tags
                int numTags = 2 + random.nextInt(4);
                List<String> postTags = new ArrayList<>();
                for (int j = 0; j < numTags; j++) {
                    postTags.add(tags.get(random.nextInt(tags.size())));
                }

                String tagsJson = "["
                        + String.join(
                                ",",
                                postTags.stream()
                                        .map(t -> "{\"tagName\":\"" + t + "\"}")
                                        .toList()) + "]";
                String json = "{\"title\":\"Post " + i + "\", \"content\":\"Content for post " + i
                        + "\", \"authorEmail\":\"" + author
                        + "\", \"details\":{\"detailsKey\":\"Test details\",\"createdBy\":\"DataGenerator\"}, \"tags\":"
                        + tagsJson + "}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(LoadTestConfig.BASE_URL + "/api/posts"))
                        .header("Content-Type", "application/json")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 201) {
                    String location = response.headers().firstValue("Location").orElse("");
                    String postId = location.substring(location.lastIndexOf('/') + 1);
                    posts.add(postId);

                    // Assign weight based on index for skewed read access (hot posts)
                    int weight = count - i; // Older posts get higher weight, just an arbitrary skewed distribution
                    writer.println(postId + "," + weight);

                    for (String t : postTags) {
                        tagWriter.println(t + "," + postId);
                    }
                } else {
                    System.err.println("Failed to create post: " + response.statusCode() + " " + response.body());
                }
            }
        }
        return posts;
    }

    /**
     * Creates comments across the generated posts.
     *
     * @param count the number of comments to request
     * @param posts available post identifiers
     * @param authors available author emails
     * @throws Exception when an HTTP request fails
     */
    private static void generateComments(int count, List<String> posts, List<String> authors) throws Exception {
        System.out.println("Generating " + count + " comments...");
        for (int i = 0; i < count; i++) {
            // Most posts get few comments, some get many
            String postId = posts.get(getSkewedIndex(posts.size()));
            String author = authors.get(random.nextInt(authors.size()));

            String json = "{\"title\":\"Comment " + i + "\", \"content\":\"Comment " + i + "\", \"authorEmail\":\""
                    + author + "\", \"published\":true}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LoadTestConfig.BASE_URL + "/api/posts/" + postId + "/comments"))
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                System.err.println("Failed to create comment: " + response.statusCode() + " " + response.body());
            }
        }
    }

    /**
     * Selects an index using a power distribution that favors lower values.
     *
     * @param max the exclusive upper bound
     * @return a weighted index
     */
    private static int getSkewedIndex(int max) {
        // Power distribution for skewness (x^p)
        double p = 3.0;
        double r = random.nextDouble();
        return (int) (Math.pow(r, p) * max);
    }
}
