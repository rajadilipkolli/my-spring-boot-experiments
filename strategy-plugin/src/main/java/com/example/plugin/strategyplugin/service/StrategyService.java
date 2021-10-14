package com.example.plugin.strategyplugin.service;

import com.example.plugin.strategyplugin.plugin.WriterPlugin;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

@Service
public record StrategyService(
        PluginRegistry<WriterPlugin, String> plugins) {

  public String fetchData(String type) {
    return plugins.getPluginFor(type).get().write("Hello ");
  }
}
