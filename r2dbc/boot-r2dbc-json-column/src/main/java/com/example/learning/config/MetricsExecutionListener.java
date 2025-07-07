package com.example.learning.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.r2dbc.proxy.core.MethodExecutionInfo;
import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.listener.ProxyMethodExecutionListener;
import io.r2dbc.proxy.support.QueryExecutionInfoFormatter;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsExecutionListener implements ProxyMethodExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(MetricsExecutionListener.class);

    private MeterRegistry registry;

    private String metricNamePrefix = "r2dbc.";

    private Duration slowQueryThreshold = Duration.ofSeconds(-1); // negative won't match any query

    private final QueryExecutionInfoFormatter queryFormatter =
            new QueryExecutionInfoFormatter().showTime().showConnection().showQuery();

    public MetricsExecutionListener(MeterRegistry registry) {
        this.registry = registry;
    }

    public MetricsExecutionListener(MeterRegistry registry, Duration slowQueryThreshold) {
        this.registry = registry;
        this.slowQueryThreshold = slowQueryThreshold;
    }

    @Override
    public void beforeCreateOnConnectionFactory(MethodExecutionInfo methodExecutionInfo) {
        Timer.Sample sample = Timer.start(this.registry);
        methodExecutionInfo.getValueStore().put("connectionCreate", sample);
    }

    @Override
    public void afterCreateOnConnectionFactory(MethodExecutionInfo methodExecutionInfo) {
        Timer.Sample sample = methodExecutionInfo.getValueStore().get("connectionCreate", Timer.Sample.class);

        Timer timer = Timer.builder(this.metricNamePrefix + "connection")
                .description("Time to create(acquire) a connection")
                .tags("event", "create")
                .register(this.registry);

        sample.stop(timer);
    }

    @Override
    public void afterCommitTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
        Counter counter = Counter.builder(this.metricNamePrefix + "transaction")
                .description("Num of transactions")
                .tags("event", "commit")
                .register(registry);
        counter.increment();
    }

    @Override
    public void afterRollbackTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
        incrementRollbackCounter();
    }

    @Override
    public void afterRollbackTransactionToSavepointOnConnection(MethodExecutionInfo methodExecutionInfo) {
        incrementRollbackCounter();
    }

    private void incrementRollbackCounter() {
        Counter counter = Counter.builder(this.metricNamePrefix + "transaction")
                .description("Num of transactions")
                .tags("event", "rollback")
                .register(registry);
        counter.increment();
    }

    @Override
    public void afterExecuteOnBatch(QueryExecutionInfo queryExecutionInfo) {
        afterExecuteQuery(queryExecutionInfo);
    }

    @Override
    public void afterExecuteOnStatement(QueryExecutionInfo queryExecutionInfo) {
        afterExecuteQuery(queryExecutionInfo);
    }

    private void afterExecuteQuery(QueryExecutionInfo queryExecutionInfo) {
        Counter success = Counter.builder(this.metricNamePrefix + "query")
                .description("Num of executed queries")
                .register(this.registry);
        success.increment();

        // when negative value is specified, do not log slow query
        if (this.slowQueryThreshold.isNegative()) {
            return;
        }

        if (this.slowQueryThreshold
                .minus(queryExecutionInfo.getExecuteDuration())
                .isNegative()) {
            Counter slowQueryCounter = Counter.builder(this.metricNamePrefix + "query.slow")
                    .description("Slow query count that took more than threshold")
                    .register(registry);
            slowQueryCounter.increment();

            String sb = "SlowQuery: " + this.queryFormatter.format(queryExecutionInfo);
            log.info(sb);
        }
    }

    public void setRegistry(MeterRegistry registry) {
        this.registry = registry;
    }

    public void setMetricNamePrefix(String metricNamePrefix) {
        this.metricNamePrefix = metricNamePrefix;
    }

    public void setSlowQueryThreshold(Duration slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
    }
}
