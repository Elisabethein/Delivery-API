package com.delivery_api.Delivery.API.Components;

import com.delivery_api.Delivery.API.Entities.*;
import com.delivery_api.Delivery.API.Repository.*;
import com.delivery_api.Delivery.API.Services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Component
public class BaseFeeInitializer implements ApplicationRunner {
    private final RbfRepository rbfRepository;
    @Autowired
    public BaseFeeInitializer(WpefRepository wpefRepository, RbfRepository rbfRepository) {
        this.rbfRepository = rbfRepository;
    }

    /**
     * Adding initial base fees to the database
     *
     * @param args incoming application arguments
     * @throws Exception if an error occurred
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Rbf> rbfList = Arrays.asList(
                new Rbf("Tallinn", "Car", 4.0),
                new Rbf("Tallinn", "Scooter", 3.5),
                new Rbf("Tallinn", "Bike", 3.0),
                new Rbf("Tartu", "Car", 3.5),
                new Rbf("Tartu", "Scooter", 3.0),
                new Rbf("Tartu", "Bike", 2.5),
                new Rbf("Pärnu", "Car", 3.0),
                new Rbf("Pärnu", "Scooter", 2.5),
                new Rbf("Pärnu", "Bike", 2.0)
        );
        rbfRepository.saveAll(rbfList);
    }
}
