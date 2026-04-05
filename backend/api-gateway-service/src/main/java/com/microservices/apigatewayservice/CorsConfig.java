package com.microservices.apigatewayservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // ⚠️ Ne pas utiliser "*" si allowCredentials est true
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:4200",        // pour dev local
            "http://144.126.245.171",        // frontend IP actuelle
            "https://devsecops-project.com" // futur domaine prod
        )); 

        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        config.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept"
        ));
        config.setAllowCredentials(true); // nécessaire si JWT ou cookies
        config.setMaxAge(3600L);          // cache préflight 1h

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}