package com.microservices.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
@EntityScan({
        "com.microservices.courseservice.models.entity",
        "com.microservices.commonexam.models/entity",
})
@EnableJpaRepositories(basePackages = {
        "com.microservices.courseservice.models.repository"
})
public class CourseServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(CourseServiceApplication.class, args);
    }

}
