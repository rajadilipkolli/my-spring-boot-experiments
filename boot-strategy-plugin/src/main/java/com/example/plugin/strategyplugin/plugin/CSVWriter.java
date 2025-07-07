package com.example.plugin.strategyplugin.plugin;

import com.example.plugin.strategyplugin.domain.GenericDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
class CSVWriter implements WriterPlugin {

    private static final Logger logger = LoggerFactory.getLogger(CSVWriter.class);

    @Override
    public GenericDTO write(String message) {
        logger.info("writing data for type csv with message : {}", message);
        return new GenericDTO("Writing CSV " + message);
    }

    @Override
    public boolean supports(@NonNull String delimiter) {
        return delimiter.equalsIgnoreCase("csv");
    }
}
