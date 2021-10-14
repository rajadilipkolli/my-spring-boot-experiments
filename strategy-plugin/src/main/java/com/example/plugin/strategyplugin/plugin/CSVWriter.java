package com.example.plugin.strategyplugin.plugin;

import org.springframework.stereotype.Component;

@Component
class CSVWriter implements WriterPlugin {

  @Override
  public String write(String message) {
    return "Writing CSV " + message;
  }

  @Override
  public boolean supports(String delimiter) {
    return delimiter.equalsIgnoreCase("csv");
  }
}
