package com.infratrack.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
public class StartupDiagnosticsLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnosticsLogger.class);

    private final ObjectProvider<BuildProperties> buildProperties;
    private final Environment environment;
    private final ObjectProvider<Flyway> flyway;
    private final DataSource dataSource;

    @Value("${app.operational-documents.storage-path}")
    private String storagePath;

    @Value("${server.port}")
    private int serverPort;

    public StartupDiagnosticsLogger(
            ObjectProvider<BuildProperties> buildProperties,
            Environment environment,
            ObjectProvider<Flyway> flyway,
            DataSource dataSource) {
        this.buildProperties = buildProperties;
        this.environment = environment;
        this.flyway = flyway;
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupDiagnostics() {
        BuildProperties build = buildProperties.getIfAvailable();
        String version = build != null ? build.getVersion() : "unknown";
        String profiles = Arrays.stream(environment.getActiveProfiles())
                .collect(Collectors.joining(", "));
        if (profiles.isBlank()) {
            profiles = "default";
        }

        log.info(
                "InfraTrack ready | version={} | profile={} | timezone={} | flyway={} | storage={} | database={} | port={}",
                version,
                profiles,
                TimeZone.getDefault().getID(),
                resolveFlywayVersion(),
                storagePath,
                resolveDatabaseDescription(),
                serverPort);
    }

    private String resolveFlywayVersion() {
        Flyway flywayBean = flyway.getIfAvailable();
        if (flywayBean == null) {
            return "unavailable";
        }
        var current = flywayBean.info().current();
        if (current == null || current.getVersion() == null) {
            return "none";
        }
        return current.getVersion().getVersion();
    }

    private String resolveDatabaseDescription() {
        try (Connection connection = dataSource.getConnection()) {
            var metadata = connection.getMetaData();
            String product = metadata.getDatabaseProductName();
            String catalog = connection.getCatalog();
            if (catalog != null && !catalog.isBlank()) {
                return product + " (" + catalog + ")";
            }
            return product;
        } catch (Exception ex) {
            return "unavailable";
        }
    }
}
