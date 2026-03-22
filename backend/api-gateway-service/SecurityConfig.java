package com.microservices.apigatewayservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchange -> exchange

                // routes publiques
                .pathMatchers("/courses/**").permitAll()
                .pathMatchers("/exams/**").permitAll()
                .pathMatchers("/answers/**").permitAll()
                .pathMatchers("/users/**").permitAll()

                // le reste nécessite auth
                .anyExchange().authenticated()
            )
            .httpBasic().disable()
            .formLogin().disable();

        return http.build();
    }
}