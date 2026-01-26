package com.microservices.answerservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    private static final MongoDBContainer mongoDBContainer;

    static {
        try {
            mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4"))
                    .withExposedPorts(27017)
                    .withReuse(true);
            mongoDBContainer.start();
            System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        } catch (Exception e) {
            System.out.println("⚠️ TestContainers Docker not available. Falling back to local MongoDB.");
            System.out.println("Error: " + e.getMessage());
            // Fall back to local MongoDB on localhost:27017
            System.setProperty("spring.data.mongodb.uri", "mongodb://localhost:27017/answer-service");
        }
    }

    @Bean
    public MongoDBContainer mongoDBContainer() {
        return mongoDBContainer;
    }
}
