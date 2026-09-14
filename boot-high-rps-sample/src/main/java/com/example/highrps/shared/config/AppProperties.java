package com.example.highrps.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NestedConfigurationProperty
    private Kafka kafka = new Kafka();

    @NestedConfigurationProperty
    private Batch batch = new Batch();

    @NestedConfigurationProperty
    private Cache cache = new Cache();

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    /**
     * Returns the local cache settings.
     *
     * @return the cache settings
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * Replaces the local cache settings.
     *
     * @param cache the cache settings to use
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public static class Cache {
        private long localMaxSize = 10000;

        /**
         * Returns the maximum number of entries retained by the local cache.
         *
         * @return the local cache entry limit
         */
        public long getLocalMaxSize() {
            return localMaxSize;
        }

        /**
         * Sets the maximum number of entries retained by the local cache.
         *
         * @param localMaxSize the local cache entry limit
         */
        public void setLocalMaxSize(long localMaxSize) {
            this.localMaxSize = localMaxSize;
        }
    }

    public static class Kafka {
        private long publishTimeOutMs = 5000;
        private String minInsyncReplicas = "1";

        private Topic postsAggregatesTopic = new Topic(3, (short) 1, null);
        private Topic authorsAggregatesTopic = new Topic(3, (short) 1, null);
        private Topic postCommentsAggregatesTopic = new Topic(3, (short) 1, null);

        public long getPublishTimeOutMs() {
            return publishTimeOutMs;
        }

        public void setPublishTimeOutMs(long publishTimeOutMs) {
            this.publishTimeOutMs = publishTimeOutMs;
        }

        public String getMinInsyncReplicas() {
            return minInsyncReplicas;
        }

        public void setMinInsyncReplicas(String minInsyncReplicas) {
            this.minInsyncReplicas = minInsyncReplicas;
        }

        public Topic getPostsAggregatesTopic() {
            return postsAggregatesTopic;
        }

        public void setPostsAggregatesTopic(Topic postsAggregatesTopic) {
            this.postsAggregatesTopic = postsAggregatesTopic;
        }

        public Topic getAuthorsAggregatesTopic() {
            return authorsAggregatesTopic;
        }

        public void setAuthorsAggregatesTopic(Topic authorsAggregatesTopic) {
            this.authorsAggregatesTopic = authorsAggregatesTopic;
        }

        public Topic getPostCommentsAggregatesTopic() {
            return postCommentsAggregatesTopic;
        }

        public void setPostCommentsAggregatesTopic(Topic postCommentsAggregatesTopic) {
            this.postCommentsAggregatesTopic = postCommentsAggregatesTopic;
        }

        public static class Topic {
            private int partitions;
            private short replicationFactor;
            private Long tombstoneRetentionMs;

            public Topic() {}

            public Topic(int partitions, short replicationFactor, Long tombstoneRetentionMs) {
                this.partitions = partitions;
                this.replicationFactor = replicationFactor;
                this.tombstoneRetentionMs = tombstoneRetentionMs;
            }

            public int getPartitions() {
                return partitions;
            }

            public void setPartitions(int partitions) {
                this.partitions = partitions;
            }

            public short getReplicationFactor() {
                return replicationFactor;
            }

            public void setReplicationFactor(short replicationFactor) {
                this.replicationFactor = replicationFactor;
            }

            public Long getTombstoneRetentionMs() {
                return tombstoneRetentionMs;
            }

            public void setTombstoneRetentionMs(Long tombstoneRetentionMs) {
                this.tombstoneRetentionMs = tombstoneRetentionMs;
            }
        }
    }

    public static class Batch {
        private String queueKey = "events:queue";
        private int size = 5000;
        private long delayMs = 500;

        public String getQueueKey() {
            return queueKey;
        }

        public void setQueueKey(String queueKey) {
            this.queueKey = queueKey;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getDelayMs() {
            return delayMs;
        }

        public void setDelayMs(long delayMs) {
            this.delayMs = delayMs;
        }
    }
}
