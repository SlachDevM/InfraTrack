package com.infratrack.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerProductionLockdownTest {

    @Test
    void prodProfileProperties_shouldDisableSpringdoc() throws IOException {
        Properties properties = loadProperties("application-prod.properties");

        assertThat(properties.getProperty("springdoc.api-docs.enabled")).isEqualTo("false");
        assertThat(properties.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("false");
    }

    @Test
    void defaultProperties_shouldEnableSpringdocForDevelopment() throws IOException {
        Properties properties = loadProperties("application.properties");

        assertThat(properties.getProperty("springdoc.api-docs.enabled")).isEqualTo("true");
        assertThat(properties.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("true");
    }

    private Properties loadProperties(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
            properties.load(inputStream);
        }
        return properties;
    }
}
