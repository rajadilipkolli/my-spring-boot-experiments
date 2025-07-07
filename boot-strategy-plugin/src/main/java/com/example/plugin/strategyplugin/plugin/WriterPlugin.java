package com.example.plugin.strategyplugin.plugin;

import com.example.plugin.strategyplugin.domain.GenericDTO;
import org.springframework.plugin.core.Plugin;

public interface WriterPlugin extends Plugin<String> {
    GenericDTO write(String message);
}
