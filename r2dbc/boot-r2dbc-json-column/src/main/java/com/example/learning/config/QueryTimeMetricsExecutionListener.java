package com.example.learning.config;

import static java.lang.String.format;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.core.QueryInfo;
import io.r2dbc.proxy.listener.ProxyExecutionListener;

/**
 * Listener to populate micrometer metrics for query execution.
 * <p>
 * Create time metrics for query execution by type(read/write).
 * <a href="https://github.com/micrometer-metrics/micrometer/issues/635">...</a>
 *
 * @author Tadaya Tsuyukubo
 */
public class QueryTimeMetricsExecutionListener implements ProxyExecutionListener {

    private MeterRegistry registry;

    private String metricNamePrefix = "r2dbc.";

    private QueryTypeDetector queryTypeDetector = new DefaultQueryTypeDetector();

    public QueryTimeMetricsExecutionListener(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterQuery(QueryExecutionInfo queryExecutionInfo) {
        for (QueryInfo queryInfo : queryExecutionInfo.getQueries()) {
            String queryType =
                    this.queryTypeDetector.detect(queryInfo.getQuery()).name().toLowerCase();
            String metricsName = this.metricNamePrefix + "query." + queryType;
            String description = format("Time to execute %s queries", queryType);

            Timer timer = Timer.builder(metricsName)
                    .description(description)
                    .tags("event", "query")
                    .register(this.registry);
            timer.record(queryExecutionInfo.getExecuteDuration());
        }
    }

    public void setRegistry(MeterRegistry registry) {
        this.registry = registry;
    }

    public void setMetricNamePrefix(String metricNamePrefix) {
        this.metricNamePrefix = metricNamePrefix;
    }

    public void setQueryTypeDetector(QueryTypeDetector queryTypeDetector) {
        this.queryTypeDetector = queryTypeDetector;
    }

    public enum QueryType {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        OTHER
    }

    public interface QueryTypeDetector {
        QueryType detect(String query);
    }

    public static class DefaultQueryTypeDetector implements QueryTypeDetector {
        @Override
        public QueryType detect(String query) {
            final String trimmedQuery = removeCommentAndWhiteSpace(query);
            if (trimmedQuery == null || trimmedQuery.length() < 6) {
                return QueryType.OTHER;
            }

            String prefix = trimmedQuery.substring(0, 6).toUpperCase();
            final QueryType type;
            switch (prefix) {
                case "SELECT":
                    type = QueryType.SELECT;
                    break;
                case "INSERT":
                    type = QueryType.INSERT;
                    break;
                case "UPDATE":
                    type = QueryType.UPDATE;
                    break;
                case "DELETE":
                    type = QueryType.DELETE;
                    break;
                default:
                    type = QueryType.OTHER;
            }
            return type;
        }

        private String removeCommentAndWhiteSpace(String query) {
            if (query == null) {
                return null;
            }
            return query.replaceAll("--.*\n", "")
                    .replaceAll("\n", "")
                    .replaceAll("/\\*.*\\*/", "")
                    .trim();
        }
    }
}
