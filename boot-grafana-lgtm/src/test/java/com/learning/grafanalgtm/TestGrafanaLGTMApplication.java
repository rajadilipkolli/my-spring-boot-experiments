package com.learning.grafanalgtm;

import com.learning.grafanalgtm.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestGrafanaLGTMApplication {

    public static void main(String[] args) {
        SpringApplication.from(GrafanaLGTMApplication::main)
                .with(ContainerConfig.class)
                .run(args);
    }
}
