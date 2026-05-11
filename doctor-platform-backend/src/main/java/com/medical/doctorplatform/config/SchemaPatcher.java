package com.medical.doctorplatform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 为已有库增量补列，避免手工执行 SQL。
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class SchemaPatcher implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfMissing("doctor", "role",
                "ALTER TABLE doctor ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'DOCTOR'");
        addColumnIfMissing("elder", "creator_doctor_id",
                "ALTER TABLE elder ADD COLUMN creator_doctor_id BIGINT NULL");
        addColumnIfMissing("health_alert", "doctor_id",
                "ALTER TABLE health_alert ADD COLUMN doctor_id BIGINT NULL");
    }

    private void addColumnIfMissing(String table, String column, String alterSql) {
        Integer n = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND LOWER(table_name) = LOWER(?)
                          AND LOWER(column_name) = LOWER(?)
                        """,
                Integer.class,
                table,
                column);
        if (n != null && n == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }
}
