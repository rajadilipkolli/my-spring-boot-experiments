package com.example.jooq.r2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;

@Configuration
@EnableR2dbcAuditing
public class JooqConfiguration {

    @Bean
    public DSLContext dslContext(ConnectionFactory connectionFactory) {
        return DSL.using(
                new TransactionAwareConnectionFactoryProxy(connectionFactory), SQLDialect.POSTGRES);
    }
}
