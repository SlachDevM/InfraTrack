package com.infratrack.config;

import com.infratrack.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(com.infratrack.security.JwtAuthenticationEntryPoint.class));
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", "http://localhost:3000");
        ReflectionTestUtils.setField(securityConfig, "swaggerUiEnabled", true);
        ReflectionTestUtils.setField(securityConfig, "apiDocsEnabled", true);
    }

    @Test
    void isProductionProfile_shouldBeTrueForProd() {
        ReflectionTestUtils.setField(securityConfig, "activeProfile", "prod");

        assertThat(securityConfig.isProductionProfile()).isTrue();
    }

    @Test
    void isProductionProfile_shouldBeFalseForDev() {
        ReflectionTestUtils.setField(securityConfig, "activeProfile", "dev");

        assertThat(securityConfig.isProductionProfile()).isFalse();
    }

    @Test
    void isProductionProfile_shouldBeTrueWhenProdIsCombinedWithOtherProfiles() {
        ReflectionTestUtils.setField(securityConfig, "activeProfile", "prod,cloud");

        assertThat(securityConfig.isProductionProfile()).isTrue();
    }

    @Test
    void corsConfiguration_shouldAllowMinimumRequiredHeaders() {
        ReflectionTestUtils.setField(securityConfig, "activeProfile", "dev");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/auth/login"));

        assertThat(cors.getAllowedHeaders()).containsExactlyInAnyOrder(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        );
        assertThat(cors.getExposedHeaders()).isEmpty();
    }

    @Test
    void isProductionProfile_shouldEnableHstsOnlyInProductionProfiles() {
        ReflectionTestUtils.setField(securityConfig, "activeProfile", "prod");
        assertThat(securityConfig.isProductionProfile()).isTrue();

        ReflectionTestUtils.setField(securityConfig, "activeProfile", "dev");
        assertThat(securityConfig.isProductionProfile()).isFalse();
    }
}
