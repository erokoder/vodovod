package com.vodovod.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Ensure PAYMENTS.BILL_ID is nullable to support prepayments (payments without a bill)
            List<String> results = jdbcTemplate.query(
                    "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'PAYMENTS' AND UPPER(COLUMN_NAME) = 'BILL_ID'",
                    (rs, rowNum) -> rs.getString(1)
            );
            if (!results.isEmpty()) {
                String isNullable = results.get(0);
                if ("NO".equalsIgnoreCase(isNullable)) {
                    log.info("Altering PAYMENTS.BILL_ID to be NULLABLE...");
                    jdbcTemplate.execute("ALTER TABLE PAYMENTS ALTER COLUMN BILL_ID SET NULL");
                    log.info("Schema updated: PAYMENTS.BILL_ID is now NULLABLE.");
                }
            }

            // Ensure global uniqueness for users.username and users.email (case-insensitive)
            // (App normalizes to lower-case, but DB uniqueness is the final guard)
            try {
                jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS UX_USERS_USERNAME_LC ON USERS(LOWER(USERNAME))");
            } catch (Exception e) {
                log.warn("Could not ensure UX_USERS_USERNAME_LC: {}", e.getMessage());
            }
            try {
                jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS UX_USERS_EMAIL_LC ON USERS(LOWER(EMAIL))");
            } catch (Exception e) {
                log.warn("Could not ensure UX_USERS_EMAIL_LC: {}", e.getMessage());
            }
        } catch (Exception e) {
            // Non-fatal: log and continue; this is a best-effort fix for existing databases
            log.warn("Schema migration check failed (will continue): {}", e.getMessage());
        }
    }
}

