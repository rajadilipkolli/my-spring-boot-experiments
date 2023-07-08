package com.example.plugin.strategyplugin.plugin;

import org.springframework.plugin.core.Plugin;

import com.example.plugin.strategyplugin.domain.GenericDTO;

public interface WriterPlugin extends Plugin<String> {
  GenericDTO write(String message);
}
