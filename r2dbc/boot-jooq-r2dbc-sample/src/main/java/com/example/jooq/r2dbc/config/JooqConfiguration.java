package com.example.jooq.r2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.autoconfigure.jooq.ExceptionTranslatorExecuteListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@EnableR2dbcAuditing(auditorAwareRef = "myAuditorProvider", modifyOnCreate = false)
public class JooqConfiguration {

    @Bean
    public ReactiveAuditorAware<String> myAuditorProvider() {
        return () -> Mono.justOrEmpty("appUser");
    }

    @Bean
    DSLContext dslContext(ConnectionFactory connectionFactory) {
        return DSL.using(new TransactionAwareConnectionFactoryProxy(connectionFactory)).dsl();
    }

    @Bean
    @Order(0)
    DefaultExecuteListenerProvider jooqExceptionTranslatorExecuteListenerProvider() {
        return new DefaultExecuteListenerProvider(ExceptionTranslatorExecuteListener.DEFAULT);
    }
}
