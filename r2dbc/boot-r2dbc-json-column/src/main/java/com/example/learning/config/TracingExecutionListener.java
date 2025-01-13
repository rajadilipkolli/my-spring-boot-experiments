package com.example.learning.config;

import static java.util.stream.Collectors.joining;

import brave.Span;
import brave.Tracer;
import io.r2dbc.proxy.core.ConnectionInfo;
import io.r2dbc.proxy.core.ExecutionType;
import io.r2dbc.proxy.core.MethodExecutionInfo;
import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.core.QueryInfo;
import io.r2dbc.proxy.listener.ProxyMethodExecutionListener;

/**
 * Listener to create spans for R2DBC SPI operations.
 *
 * @author Tadaya Tsuyukubo
 */
public class TracingExecutionListener implements ProxyMethodExecutionListener {

    private static final String TAG_CONNECTION_ID = "connectionId";
    private static final String TAG_CONNECTION_CREATE_THREAD_ID = "threadIdOnCreate";
    private static final String TAG_CONNECTION_CLOSE_THREAD_ID = "threadIdOnClose";
    private static final String TAG_CONNECTION_CREATE_THREAD_NAME = "threadNameOnCreate";
    private static final String TAG_CONNECTION_CLOSE_THREAD_NAME = "threadNameOnClose";
    private static final String TAG_THREAD_ID = "threadId";
    private static final String TAG_THREAD_NAME = "threadName";
    private static final String TAG_QUERIES = "queries";
    private static final String TAG_BATCH_SIZE = "batchSize";
    private static final String TAG_QUERY_TYPE = "type";
    private static final String TAG_QUERY_SUCCESS = "success";
    private static final String TAG_QUERY_MAPPED_RESULT_COUNT = "mappedResultCount";
    private static final String TAG_TRANSACTION_SAVEPOINT = "savepoint";
    private static final String TAG_TRANSACTION_COUNT = "transactionCount";
    private static final String TAG_COMMIT_COUNT = "commitCount";
    private static final String TAG_ROLLBACK_COUNT = "rollbackCount";

    static final String CONNECTION_SPAN_KEY = "connectionSpan";
    static final String TRANSACTION_SPAN_KEY = "transactionSpan";
    static final String QUERY_SPAN_KEY = "querySpan";

    private final Tracer tracer;

    public TracingExecutionListener(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void beforeCreateOnConnectionFactory(MethodExecutionInfo methodExecutionInfo) {
        Span connectionSpan = this.tracer
                .nextSpan()
                .name("r2dbc:connection")
                .kind(Span.Kind.CLIENT)
                .start();

        // store the span for retrieval at "afterCreateOnConnectionFactory"
        methodExecutionInfo.getValueStore().put("initialConnectionSpan", connectionSpan);
    }

    @Override
    public void afterCreateOnConnectionFactory(MethodExecutionInfo methodExecutionInfo) {
        // retrieve the span created at "beforeCreateOnConnectionFactory"
        Span connectionSpan = methodExecutionInfo.getValueStore().get("initialConnectionSpan", Span.class);

        Throwable thrown = methodExecutionInfo.getThrown();
        if (thrown != null) {
            connectionSpan.error(thrown).finish();
            return;
        }

        ConnectionInfo connectionInfo = methodExecutionInfo.getConnectionInfo();
        String connectionId = connectionInfo.getConnectionId();

        connectionSpan
                .tag(TAG_CONNECTION_ID, connectionId)
                .tag(TAG_CONNECTION_CREATE_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
                .tag(TAG_CONNECTION_CREATE_THREAD_NAME, methodExecutionInfo.getThreadName())
                .annotate("Connection created");

        // store the span in connection scoped value store
        connectionInfo.getValueStore().put(CONNECTION_SPAN_KEY, connectionSpan);
    }

    @Override
    public void afterCloseOnConnection(MethodExecutionInfo methodExecutionInfo) {
        ConnectionInfo connectionInfo = methodExecutionInfo.getConnectionInfo();
        String connectionId = connectionInfo.getConnectionId();
        Span connectionSpan = connectionInfo.getValueStore().get(CONNECTION_SPAN_KEY, Span.class);
        if (connectionSpan == null) {
            return; // already closed
        }
        Throwable thrown = methodExecutionInfo.getThrown();
        if (thrown != null) {
            connectionSpan.error(thrown);
        }
        connectionSpan
                .tag(TAG_CONNECTION_ID, connectionId)
                .tag(TAG_CONNECTION_CLOSE_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
                .tag(TAG_CONNECTION_CLOSE_THREAD_NAME, methodExecutionInfo.getThreadName())
                .tag(TAG_TRANSACTION_COUNT, String.valueOf(connectionInfo.getTransactionCount()))
                .tag(TAG_COMMIT_COUNT, String.valueOf(connectionInfo.getCommitCount()))
                .tag(TAG_ROLLBACK_COUNT, String.valueOf(connectionInfo.getRollbackCount()))
                .finish();
    }

    @Override
    public void beforeQuery(QueryExecutionInfo queryExecutionInfo) {
        String connectionId = queryExecutionInfo.getConnectionInfo().getConnectionId();

        String queries = queryExecutionInfo.getQueries().stream()
                .map(QueryInfo::getQuery)
                .collect(joining(", "));

        Span querySpan = this.tracer
                .nextSpan()
                .name("r2dbc:query")
                .kind(Span.Kind.CLIENT)
                .tag(TAG_CONNECTION_ID, connectionId)
                .tag(TAG_QUERY_TYPE, queryExecutionInfo.getType().toString())
                .tag(TAG_QUERIES, queries)
                .start();

        if (ExecutionType.BATCH == queryExecutionInfo.getType()) {
            querySpan.tag(TAG_BATCH_SIZE, Integer.toString(queryExecutionInfo.getBatchSize()));
        }

        // pass the query span to "afterQuery" method
        queryExecutionInfo.getValueStore().put(QUERY_SPAN_KEY, querySpan);
    }

    @Override
    public void afterQuery(QueryExecutionInfo queryExecutionInfo) {
        Span querySpan = queryExecutionInfo.getValueStore().get(QUERY_SPAN_KEY, Span.class);
        querySpan
                .tag(TAG_THREAD_ID, String.valueOf(queryExecutionInfo.getThreadId()))
                .tag(TAG_THREAD_NAME, queryExecutionInfo.getThreadName())
                .tag(TAG_QUERY_SUCCESS, Boolean.toString(queryExecutionInfo.isSuccess()));

        Throwable thrown = queryExecutionInfo.getThrowable();
        if (thrown != null) {
            querySpan.error(thrown);
        } else {
            querySpan.tag(TAG_QUERY_MAPPED_RESULT_COUNT, Integer.toString(queryExecutionInfo.getCurrentResultCount()));
        }
        querySpan.finish();
    }

    @Override
    public void beforeBeginTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
        Span transactionSpan = this.tracer
                .nextSpan()
                .name("r2dbc:transaction")
                .kind(Span.Kind.CLIENT)
                .start();

        methodExecutionInfo.getConnectionInfo().getValueStore().put(TRANSACTION_SPAN_KEY, transactionSpan);
    }

    @Override
    public void afterCommitTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
        ConnectionInfo connectionInfo = methodExecutionInfo.getConnectionInfo();
        String connectionId = connectionInfo.getConnectionId();

        Span transactionSpan = connectionInfo.getValueStore().get(TRANSACTION_SPAN_KEY, Span.class);
        if (transactionSpan != null) {
            transactionSpan
                    .annotate("Commit")
                    .tag(TAG_CONNECTION_ID, connectionId)
                    .tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
                    .tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName())
                    .finish();
        }

        Span connectionSpan = connectionInfo.getValueStore().get(CONNECTION_SPAN_KEY, Span.class);
        if (connectionSpan == null) {
            return;
        }
        connectionSpan.annotate("Transaction commit");
    }

    @Override
    public void afterRollbackTransactionOnConnection(MethodExecutionInfo methodExecutionInfo) {
        ConnectionInfo connectionInfo = methodExecutionInfo.getConnectionInfo();
        String connectionId = connectionInfo.getConnectionId();

        Span transactionSpan = connectionInfo.getValueStore().get(TRANSACTION_SPAN_KEY, Span.class);
        if (transactionSpan != null) {
            transactionSpan
                    .annotate("Rollback")
                    .tag(TAG_CONNECTION_ID, connectionId)
                    .tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
                    .tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName())
                    .finish();
        }

        Span connectionSpan = connectionInfo.getValueStore().get(CONNECTION_SPAN_KEY, Span.class);
        connectionSpan.annotate("Transaction rollback");
    }

    @Override
    public void afterRollbackTransactionToSavepointOnConnection(MethodExecutionInfo methodExecutionInfo) {
        ConnectionInfo connectionInfo = methodExecutionInfo.getConnectionInfo();
        String connectionId = connectionInfo.getConnectionId();
        String savepoint = (String) methodExecutionInfo.getMethodArgs()[0];

        Span transactionSpan = connectionInfo.getValueStore().get(TRANSACTION_SPAN_KEY, Span.class);
        if (transactionSpan != null) {
            transactionSpan
                    .annotate("Rollback to savepoint")
                    .tag(TAG_TRANSACTION_SAVEPOINT, savepoint)
                    .tag(TAG_CONNECTION_ID, connectionId)
                    .tag(TAG_THREAD_ID, String.valueOf(methodExecutionInfo.getThreadId()))
                    .tag(TAG_THREAD_NAME, methodExecutionInfo.getThreadName())
                    .finish();
        }

        Span connectionSpan = connectionInfo.getValueStore().get(CONNECTION_SPAN_KEY, Span.class);
        connectionSpan.annotate("Transaction rollback to savepoint");
    }
}
