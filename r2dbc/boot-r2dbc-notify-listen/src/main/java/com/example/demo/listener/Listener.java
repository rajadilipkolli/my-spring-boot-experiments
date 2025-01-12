package com.example.demo.listener;

import io.r2dbc.postgresql.api.PostgresqlConnection;
import io.r2dbc.postgresql.api.PostgresqlResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class Listener {

    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    private final PostgresqlConnection receiver;

    public Listener(PostgresqlConnection receiver) {
        this.receiver = receiver;
    }

    @PostConstruct
    public void initialize() {
        receiver.createStatement("LISTEN mymessage")
                .execute()
                .flatMap(PostgresqlResult::getRowsUpdated)
                .log("listen::")
                .subscribe();

        receiver.getNotifications()
                .delayElements(Duration.ofSeconds(1))
                .subscribe(notification -> log.info(
                        "Received notification: with name :{} , processId :{} and parameter :{}",
                        notification.getName(),
                        notification.getProcessId(),
                        notification.getParameter()));
    }

    @PreDestroy
    public void destroy() {
        receiver.close()
                .doOnSuccess(v -> log.info("Listener connection closed"))
                .subscribe();
    }
}
