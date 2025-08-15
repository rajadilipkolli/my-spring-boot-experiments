package com.example.rest.proxy;

import com.example.rest.proxy.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

class TestHttpProxyApplication {

    public static void main(String[] args) {
        SpringApplication.from(HttpProxyApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
