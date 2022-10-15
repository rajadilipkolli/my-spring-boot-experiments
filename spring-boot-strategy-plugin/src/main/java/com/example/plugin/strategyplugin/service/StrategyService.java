package com.example.plugin.strategyplugin.service;

import com.example.plugin.strategyplugin.plugin.WriterPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

@Service
public class StrategyService {

    private static final Logger logger = LoggerFactory.getLogger(StrategyService.class);

    private final PluginRegistry<WriterPlugin, String> plugins;

    public StrategyService(PluginRegistry<WriterPlugin, String> plugins) {
        this.plugins = plugins;
    }

    public String fetchData(String type) {
        logger.info("fetching data for type :{}", type);
        return plugins.getPluginFor(type).get().write("Hello ");
    }
}
