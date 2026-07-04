package com.infratrack.config.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OperationalDocumentStorageHealthIndicatorTest {

    @TempDir
    Path tempDir;

    @Test
    void health_shouldReportUpWhenDirectoryIsWritable() throws Exception {
        Path storage = Files.createDirectory(tempDir.resolve("documents"));
        OperationalDocumentStorageHealthIndicator indicator =
                new OperationalDocumentStorageHealthIndicator(storage.toString());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("path", storage.toAbsolutePath().normalize().toString());
    }

    @Test
    void health_shouldReportDownWhenDirectoryDoesNotExist() {
        Path missing = tempDir.resolve("missing");
        OperationalDocumentStorageHealthIndicator indicator =
                new OperationalDocumentStorageHealthIndicator(missing.toString());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "directory does not exist");
    }

    @Test
    void health_shouldReportDownWhenPathIsAFile() throws Exception {
        Path file = Files.createFile(tempDir.resolve("not-a-directory"));
        OperationalDocumentStorageHealthIndicator indicator =
                new OperationalDocumentStorageHealthIndicator(file.toString());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "path is not a directory");
    }
}
