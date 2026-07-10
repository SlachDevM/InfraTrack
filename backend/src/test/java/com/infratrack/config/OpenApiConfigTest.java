package com.infratrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void infratrackOpenApi_shouldExposeBuildPropertiesVersion() {
        BuildProperties buildProperties = new BuildProperties(new Properties() {{
            setProperty("version", "2.0.1");
        }});

        OpenAPI openAPI = new OpenApiConfig().infratrackOpenApi("InfraTrack", buildProperties);

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("2.0.1");
    }

    @Test
    void infratrackOpenApi_shouldTrackBuildPropertiesWhenVersionChanges() {
        BuildProperties buildProperties = new BuildProperties(new Properties() {{
            setProperty("version", "2.0.2");
        }});

        OpenAPI openAPI = new OpenApiConfig().infratrackOpenApi("InfraTrack", buildProperties);

        assertThat(openAPI.getInfo().getVersion()).isEqualTo("2.0.2");
    }
}
