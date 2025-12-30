package com.example.demo;

import com.example.demo.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

class TestNotifyListenPGApplication {

    static void main(String[] args) {
        SpringApplication.from(NotifyListenPGApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
