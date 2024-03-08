package com.delivery_api.Delivery.API.Repository;

import com.delivery_api.Delivery.API.Entities.Wpef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WpefRepository extends JpaRepository<Wpef, Long> {
    Wpef findByContaining(String string);
}
