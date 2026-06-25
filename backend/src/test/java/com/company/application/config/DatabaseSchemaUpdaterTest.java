package com.company.application.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DatabaseSchemaUpdaterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DatabaseSchemaUpdater schemaUpdater;

    @Test
    void onApplicationEvent_addsEnabledColumnIfNotExists() {
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        schemaUpdater.onApplicationEvent(event);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).execute(sqlCaptor.capture());

        String addEnabledColumn = sqlCaptor.getAllValues().stream()
                .filter(sql -> sql.contains("ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE"))
                .findFirst()
                .orElse(null);

        assertThat(addEnabledColumn)
                .as("Should add enabled column with default value TRUE for backward compatibility")
                .isNotNull();
    }

    @Test
    void onApplicationEvent_migratesOnlyNullEnabledToTrue() {
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        schemaUpdater.onApplicationEvent(event);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).execute(sqlCaptor.capture());

        String updateExistingUsers = sqlCaptor.getAllValues().stream()
                .filter(sql -> sql.contains("UPDATE users SET enabled = TRUE"))
                .findFirst()
                .orElse(null);

        assertThat(updateExistingUsers)
                .as("Should update only NULL enabled values to preserve intentionally disabled users")
                .isNotNull()
                .contains("WHERE enabled IS NULL")
                .doesNotContain("enabled = FALSE");
    }

    @Test
    void onApplicationEvent_preservesIntentionallyDisabledUsers() {
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        schemaUpdater.onApplicationEvent(event);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).execute(sqlCaptor.capture());

        String migrationSql = sqlCaptor.getAllValues().stream()
                .filter(sql -> sql.contains("UPDATE users SET enabled = TRUE"))
                .findFirst()
                .orElse(null);

        assertThat(migrationSql)
                .as("Migration should NOT include 'enabled = FALSE' to avoid reactivating explicitly disabled users")
                .doesNotContain("enabled = FALSE");
    }

    @Test
    void onApplicationEvent_addsFcmTokenColumn() {
        ApplicationReadyEvent event = mock(ApplicationReadyEvent.class);

        schemaUpdater.onApplicationEvent(event);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).execute(sqlCaptor.capture());

        String addFcmColumn = sqlCaptor.getAllValues().stream()
                .filter(sql -> sql.contains("ADD COLUMN IF NOT EXISTS fcm_token TEXT"))
                .findFirst()
                .orElse(null);

        assertThat(addFcmColumn)
                .as("Should add fcm_token column for Firebase Cloud Messaging")
                .isNotNull();
    }
}
