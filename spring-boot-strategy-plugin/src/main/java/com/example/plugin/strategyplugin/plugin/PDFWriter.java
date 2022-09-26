package com.example.plugin.strategyplugin.plugin;

import org.springframework.stereotype.Component;

@Component
class PDFWriter implements WriterPlugin {

  @Override
  public String write(String message) {
    return "Writing pdf " + message;
  }

  @Override
  public boolean supports(String delimiter) {
    return delimiter.equalsIgnoreCase("pdf");
  }
}
