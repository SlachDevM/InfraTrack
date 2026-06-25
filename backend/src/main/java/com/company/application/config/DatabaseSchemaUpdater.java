package com.company.application.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaUpdater implements ApplicationListener<ApplicationReadyEvent> {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token TEXT");

        // Ensure existing users remain enabled (backward compatibility)
        // The DEFAULT TRUE handles any NEW users created by JPA without explicit enabled value
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE");
        // CRITICAL: Only migrate NULL values (existing users from before this feature)
        // Do NOT reactivate users that were explicitly disabled (enabled = FALSE)
        jdbcTemplate.execute("UPDATE users SET enabled = TRUE WHERE enabled IS NULL");
    }
}
