package com.example.plugin.strategyplugin.plugin;

import org.springframework.plugin.core.Plugin;

public interface WriterPlugin extends Plugin<String> {
  String write(String message);
}
