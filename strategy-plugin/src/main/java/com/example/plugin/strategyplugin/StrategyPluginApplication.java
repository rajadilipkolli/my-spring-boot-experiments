package com.example.plugin.strategyplugin;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.plugin.core.Plugin;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.stereotype.Component;

import java.util.List;

@SpringBootApplication
@EnablePluginRegistries({WriterPlugin.class})
public class StrategyPluginApplication {

  public static void main(String[] args) {
    SpringApplication.run(StrategyPluginApplication.class, args);
  }

  @Bean
  ApplicationRunner runner(PluginRegistry<WriterPlugin, String> plugins) {
    return args -> {
      List.of("csv", "pdf")
          .forEach(
              s -> {
                plugins.getPluginFor(s).get().write("Hello ");
              });
    };
  }
}

@Component
class CSVWriter implements WriterPlugin {

  @Override
  public void write(String message) {
    System.out.println("Writing CSV " + message);
  }

  @Override
  public boolean supports(String delimiter) {
    return delimiter.equalsIgnoreCase("csv");
  }
}

@Component
class PDFWriter implements WriterPlugin {

  @Override
  public void write(String message) {
    System.out.println("Writing pdf " + message);
  }

  @Override
  public boolean supports(String delimiter) {
    return delimiter.equalsIgnoreCase("pdf");
  }
}

interface WriterPlugin extends Plugin<String> {
  void write(String message);
}
