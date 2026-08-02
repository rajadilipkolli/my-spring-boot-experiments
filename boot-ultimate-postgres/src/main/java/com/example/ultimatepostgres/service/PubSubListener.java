package com.example.ultimatepostgres.service;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PubSubListener {

    private static final Logger log = LoggerFactory.getLogger(PubSubListener.class);
    private static final String CHANNEL = "ultimate_channel";

    private final DataSource dataSource;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
    private Thread listenerThread;

    public PubSubListener(DataSource dataSource) {
        this.dataSource = dataSource;
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            int maxPoolSize = hikariDataSource.getMaximumPoolSize();
            if (maxPoolSize < 11) {
                log.warn(
                        "Hikari pool size is {}, which might be too small for dedicated LISTEN connection",
                        maxPoolSize);
            }
        }
    }

    @PostConstruct
    public void start() {
        running.set(true);
        listenerThread = new Thread(this::listen);
        listenerThread.setDaemon(true);
        listenerThread.setName("PubSubListener");
        listenerThread.start();
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }

    private void listen() {
        while (running.get()) {
            try (Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement()) {

                PGConnection pgConn = conn.unwrap(PGConnection.class);
                stmt.execute("LISTEN " + CHANNEL);
                log.info("Listening on channel: {}", CHANNEL);

                while (running.get()) {
                    PGNotification[] notifications = pgConn.getNotifications(1000);
                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            String payload = notification.getParameter();
                            log.info("Received notification on {}: {}", notification.getName(), payload);
                            synchronized (receivedMessages) {
                                receivedMessages.add(payload);
                                if (receivedMessages.size() > 100) {
                                    receivedMessages.remove(0);
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                if (running.get()) {
                    log.error("PubSubListener error, retrying in 5 seconds...", e);
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    public List<String> getReceivedMessages() {
        return new ArrayList<>(receivedMessages);
    }
}
