package com.aminah.elearning.config;

import com.zaxxer.hikari.HikariDataSource;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
@Configuration
public class EmbeddedPostgresConfig {

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres.builder()
                .setPort(0) // auto port
                .start();
    }

    @Bean
    public DataSource dataSource(EmbeddedPostgres pg) {
        String url = "jdbc:postgresql://localhost:" + pg.getPort() + "/postgres";

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername("postgres");
        ds.setPassword("postgres");
        return ds;
    }
}

