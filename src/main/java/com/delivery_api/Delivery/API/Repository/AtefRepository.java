package com.delivery_api.Delivery.API.Repository;

import com.delivery_api.Delivery.API.Entities.Atef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtefRepository extends JpaRepository<Atef, Long> {
    Atef findByBorders(String interval);
}
