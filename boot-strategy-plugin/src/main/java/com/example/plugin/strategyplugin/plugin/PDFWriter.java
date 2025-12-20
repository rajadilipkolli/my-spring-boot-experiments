package com.example.plugin.strategyplugin.plugin;

import com.example.plugin.strategyplugin.domain.GenericDTO;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class PDFWriter implements WriterPlugin {

    private static final Logger logger = LoggerFactory.getLogger(PDFWriter.class);

    @Override
    public GenericDTO write(String message) {
        logger.info("writing data for type pdf with message :{}", message);
        return new GenericDTO("Writing pdf " + message);
    }

    @Override
    public boolean supports(@NonNull String delimiter) {
        return delimiter.equalsIgnoreCase("pdf");
    }
}
