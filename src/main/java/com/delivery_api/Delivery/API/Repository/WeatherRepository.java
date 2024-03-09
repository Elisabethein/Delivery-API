package com.delivery_api.Delivery.API.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.delivery_api.Delivery.API.Entities.Weather;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    List<Weather> findAll();

    Optional<Weather> findById(Long id);

    Weather save(Weather weather);
}
