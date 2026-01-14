package com.microservices.users.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

@Testcontainers
@TestConfiguration
public class TestContainersConfig {
    private static final Logger log = LoggerFactory.getLogger(TestContainersConfig.class);

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        log.info("Initializing PostgreSQL container...");
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withStartupTimeout(java.time.Duration.ofSeconds(60));
        postgres.start();
        if (!postgres.isRunning()) {
            log.error("Failed to start PostgreSQL container");
            throw new IllegalStateException("PostgreSQL container failed to start");
        }
        log.info("PostgreSQL container started. JDBC URL: {}", postgres.getJdbcUrl());
        return postgres;
    }

    @Bean
    public DataSource dataSource(PostgreSQLContainer<?> postgreSQLContainer) {
        log.info("Configuring DataSource with JDBC URL: {}", postgreSQLContainer.getJdbcUrl());
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(postgreSQLContainer.getJdbcUrl());
        dataSource.setUsername(postgreSQLContainer.getUsername());
        dataSource.setPassword(postgreSQLContainer.getPassword());
        return dataSource;
    }
}