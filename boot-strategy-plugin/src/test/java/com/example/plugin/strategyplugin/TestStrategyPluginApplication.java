package com.example.plugin.strategyplugin;

import com.example.plugin.strategyplugin.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

class TestStrategyPluginApplication {

    public static void main(String[] args) {
        SpringApplication.from(StrategyPluginApplication::main)
                .with(ContainerConfig.class).run(args);
    }

}