package com.example.jndi.config;

import javax.sql.DataSource;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TomcatFactoryCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final ApplicationProperties applicationProperties;

    public TomcatFactoryCustomizer(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> {
            ContextResource resource = new ContextResource();

            resource.setType(DataSource.class.getName());
            resource.setName("javadb");
            resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
            resource.setProperty("driverClassName", applicationProperties.getDriverClassName());
            resource.setProperty("url", applicationProperties.getUrl());
            resource.setProperty("username", applicationProperties.getUsername());
            resource.setProperty("password", applicationProperties.getPassword());
            context.getNamingResources().addResource(resource);
        });
    }
}
