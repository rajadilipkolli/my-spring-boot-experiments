package com.example.plugin.strategyplugin;

import com.example.plugin.strategyplugin.plugin.WriterPlugin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;

@SpringBootApplication
@EnablePluginRegistries({WriterPlugin.class})
public class StrategyPluginApplication {

    public static void main(String[] args) {
        SpringApplication.run(StrategyPluginApplication.class, args);
    }
}
