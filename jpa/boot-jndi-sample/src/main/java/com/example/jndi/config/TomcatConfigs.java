package com.example.jndi.config;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jndi.JndiObjectFactoryBean;

@Configuration(proxyBeanMethods = false)
public class TomcatConfigs {

    private final ApplicationProperties applicationProperties;

    public TomcatConfigs(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    @DependsOn("tomcatFactory")
    DataSource dataSource() throws IllegalArgumentException, NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:comp/env/jdbc/myDatasourceName");
        bean.setProxyInterface(DataSource.class);
        bean.afterPropertiesSet();

        return (DataSource) bean.getObject();
    }

    @Bean
    TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                ContextResource resource = new ContextResource();
                // resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
                resource.setName("jdbc/myDatasourceName");
                resource.setType(DataSource.class.getName());
                resource.setProperty("factory", "com.zaxxer.hikari.HikariJNDIFactory");
                resource.setProperty("autoCommit", "false");
                resource.setProperty("driverClassName", applicationProperties.getDriverClassName());
                resource.setProperty("jdbcUrl", applicationProperties.getUrl());
                resource.setProperty("username", applicationProperties.getUsername());
                resource.setProperty("password", applicationProperties.getPassword());
                context.getNamingResources().addResource(resource);
            }
        };
    }
}
