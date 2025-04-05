package com.example.jooq.r2dbc.config;

import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.listener.ProxyMethodExecutionListener;
import io.r2dbc.proxy.support.QueryExecutionInfoFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QueryProxyExecutionListener implements ProxyMethodExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(QueryProxyExecutionListener.class);

    @Override
    public void afterQuery(QueryExecutionInfo queryExecutionInfo) {
        QueryExecutionInfoFormatter formatter = QueryExecutionInfoFormatter.showAll();
        String str = formatter.format(queryExecutionInfo);
        logger.info("Query: {}", str);
        logger.info("Execution Time: {} ms", queryExecutionInfo.getExecuteDuration().toMillis());
    }
}
