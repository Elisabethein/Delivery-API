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
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private AtefRepository atefRepository;
    @Autowired
    private WsefRepository wsefRepository;
    @Autowired
    private WpefRepository wpefRepository;
    @Autowired
    private RbfRepository rbfRepository;
    @Autowired
    private WeatherRepository weatherRepository;

    /**
     * Adding initial data to the database
     *
     * @param args incoming application arguments
     * @throws Exception if an error occurred
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Add most recent weather data to the database
        weatherService.fetchWeatherData();
        // Add regional base fees to the database
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
        // Add air temperature extra fees to the database
        List<Atef> atefList = Arrays.asList(
                new Atef("<-10", 1.0),
                new Atef("-10-0", 0.5)
        );
        atefRepository.saveAll(atefList);
        // Add wind speed extra fees to the database
        List<Wsef> wsefList = Arrays.asList(
                new Wsef("10-20", 0.5),
                new Wsef(">20", -1)
        );
        wsefRepository.saveAll(wsefList);
        // Add weather phenomenon extra fees to the database
        List<Wpef> wpefList = Arrays.asList(
                new Wpef("rain", 0.5),
                new Wpef("snow", 1),
                new Wpef("sleet", 1),
                new Wpef("glaze", -1),
                new Wpef("thunder", -1),
                new Wpef("hail", -1)
        );
        wpefRepository.saveAll(wpefList);
        // Adding extra weather data to the database for the user to test the applications functionalities with different weather conditions
        List<Weather> weatherList = Arrays.asList(
                new Weather("Tallinn-Harku", "26038", -10.5, 5.0, "hail", Timestamp.valueOf("2024-02-16 07:15:00")),
                new Weather("Tallinn-Harku", "26038", 5.0, 0.7, "", Timestamp.valueOf("2024-02-20 15:15:00")),
                new Weather("Tallinn-Harku", "26038", -5.0, 5.0, "light snow", Timestamp.valueOf("2024-03-02 10:15:00")),
                new Weather("Tartu-Tõravere", "26242", -5.0, 5.0, "", Timestamp.valueOf("2024-02-16 07:15:00")),
                new Weather("Tartu-Tõravere", "26242", 5.0, 0.7, "", Timestamp.valueOf("2024-02-20 15:15:00")),
                new Weather("Tartu-Tõravere", "26242", -5.0, 5.0, "light snow", Timestamp.valueOf("2024-03-02 10:15:00")),
                new Weather("Pärnu", "41803", 0.6, 1.3, "rain", Timestamp.valueOf("2024-02-16 07:15:00")),
                new Weather("Pärnu", "41803", 5.0, 0.7, "", Timestamp.valueOf("2024-02-20 15:15:00")),
                new Weather("Pärnu", "41803", -5.0, 5.0, "light snow", Timestamp.valueOf("2024-03-02 10:15:00"))
        );
        weatherRepository.saveAll(weatherList);
    }
}
