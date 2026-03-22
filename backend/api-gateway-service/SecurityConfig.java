package com.microservices.apigatewayservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchange -> exchange

                .pathMatchers(HttpMethod.OPTIONS).permitAll()

                .pathMatchers("/courses/**").permitAll()
                .pathMatchers("/exams/**").permitAll()
                .pathMatchers("/answers/**").permitAll()
                .pathMatchers("/users/**").permitAll()

                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}