package com.infratrack.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Temporary startup schema adjustments until Flyway migrations are introduced.
 *
 * TODO: Replace this component with versioned Flyway migrations. Do not add new
 * migration logic here — extend Flyway scripts instead once Flyway is adopted.
 */
@Component
public class DatabaseSchemaUpdater implements ApplicationListener<ApplicationReadyEvent> {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token TEXT");

        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE");
        jdbcTemplate.execute("UPDATE users SET enabled = TRUE WHERE enabled IS NULL");

        migrateLegacyUserRoles();
    }

    private void migrateLegacyUserRoles() {
        jdbcTemplate.execute("UPDATE users SET role = 'ADMINISTRATOR' WHERE role = 'ADMIN'");
        jdbcTemplate.execute("UPDATE users SET role = 'FIELD_EMPLOYEE' WHERE role = 'EMPLOYEE'");
    }
}
