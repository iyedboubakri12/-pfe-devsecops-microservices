package com.microservices.courseservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    private static final MySQLContainer<?> mySQLContainer;

    static {
        mySQLContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("course-service")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        try {
            mySQLContainer.start();
            System.setProperty("spring.datasource.url", mySQLContainer.getJdbcUrl());
            System.setProperty("spring.datasource.username", "test");
            System.setProperty("spring.datasource.password", "test");
            System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
        } catch (Exception e) {
            System.err.println("TestContainers startup failed, falling back to localhost: " + e.getMessage());
            System.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/course-service-test");
            System.setProperty("spring.datasource.username", "test");
            System.setProperty("spring.datasource.password", "test");
            System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
        }
    }

    @Bean
    public MySQLContainer<?> mySQLContainer() {
        return mySQLContainer;
    }
}