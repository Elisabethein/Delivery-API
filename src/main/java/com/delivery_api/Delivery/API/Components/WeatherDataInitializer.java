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
public class WeatherDataInitializer implements ApplicationRunner {
    private  final WeatherService weatherService;
    private final WeatherRepository weatherRepository;
    @Autowired
    public WeatherDataInitializer(WeatherService weatherService, WeatherRepository weatherRepository) {
        this.weatherService = weatherService;
        this.weatherRepository = weatherRepository;
    }

    /**
     * Adding initial weather data to the database
     *
     * @param args incoming application arguments
     * @throws Exception if an error occurred
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Add most recent weather data to the database
        weatherService.fetchWeatherData();
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
