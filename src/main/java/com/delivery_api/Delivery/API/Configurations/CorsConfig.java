package com.delivery_api.Delivery.API.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * This class is responsible for configuring the CORS policy for the application.
 * It allows requests from port 5173, which is the port used by the frontend application.
 * It can be changed to allow requests from any origin.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173") // Allow requests from any origin
                .allowedMethods("*") // Allowed HTTP methods
                .allowedHeaders("*") // Allowed headers
                .allowCredentials(true);
    }
}
