package com.aminah.elearning.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationBaselineTest {

    @Test
    void baselineMigrationContainsCurrentJpaTables() throws IOException {
        String sql = new String(
                getClass().getResourceAsStream("/db/migration/V1__baseline_schema.sql").readAllBytes(),
                StandardCharsets.UTF_8
        ).toLowerCase();

        assertThat(sql).contains(
                "create table if not exists users",
                "create table if not exists courses",
                "create table if not exists course_enrollments",
                "create table if not exists payments",
                "create table if not exists section",
                "create table if not exists tutorial",
                "create table if not exists quiz_question",
                "create table if not exists quiz_options",
                "create table if not exists tutorial_progress",
                "create table if not exists verification_token",
                "create table if not exists password_reset_token",
                "create table if not exists videos",
                "create table if not exists contact"
        );
    }
}
