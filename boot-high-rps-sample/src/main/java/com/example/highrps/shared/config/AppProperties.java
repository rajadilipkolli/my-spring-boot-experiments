package com.example.highrps.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NestedConfigurationProperty
    private Kafka kafka = new Kafka();

    @NestedConfigurationProperty
    private Batch batch = new Batch();

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

    public static class Kafka {
        private long publishTimeOutMs = 5000;
        private String minInsyncReplicas = "1";

        private Topic eventsTopic = new Topic(3, (short) 1, 604800000L);
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

        public Topic getEventsTopic() {
            return eventsTopic;
        }

        public void setEventsTopic(Topic eventsTopic) {
            this.eventsTopic = eventsTopic;
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
    }
}
