package com.example.custom.sequence;

import com.example.custom.sequence.common.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestCustomSeqApplication {

    public static void main(String[] args) {
        SpringApplication.from(CustomSeqApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
