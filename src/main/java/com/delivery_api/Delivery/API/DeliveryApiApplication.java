package com.delivery_api.Delivery.API;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeliveryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryApiApplication.class, args);
    }

}
