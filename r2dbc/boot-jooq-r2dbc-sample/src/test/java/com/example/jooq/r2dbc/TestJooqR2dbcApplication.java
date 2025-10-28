package com.example.jooq.r2dbc;

import com.example.jooq.r2dbc.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

class TestJooqR2dbcApplication {

    static void main(String[] args) {
        SpringApplication.from(JooqR2dbcApplication::main).with(ContainerConfig.class).run(args);
    }
}
