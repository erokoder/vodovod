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

            // Add cancellation columns to PAYMENTS if missing
            addColumnIfMissing("PAYMENTS", "CANCELLED_AT", "TIMESTAMP");
            addColumnIfMissing("PAYMENTS", "CANCELLED_BY", "VARCHAR(255)");
            addColumnIfMissing("PAYMENTS", "CANCELLATION_REASON", "VARCHAR(1024)");
        } catch (Exception e) {
            // Non-fatal: log and continue; this is a best-effort fix for existing databases
            log.warn("Schema migration check failed (will continue): {}", e.getMessage());
        }
    }

    private void addColumnIfMissing(String tableName, String columnName, String columnType) {
        try {
            List<String> exists = jdbcTemplate.query(
                    "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = ? AND UPPER(COLUMN_NAME) = ?",
                    ps -> {
                        ps.setString(1, tableName.toUpperCase());
                        ps.setString(2, columnName.toUpperCase());
                    },
                    (rs, rowNum) -> rs.getString(1)
            );
            if (exists.isEmpty()) {
                log.info("Adding column {}.{} {} ...", tableName, columnName, columnType);
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
                log.info("Added column {}.{}", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("Could not add column {}.{}: {}", tableName, columnName, e.getMessage());
        }
    }
}

