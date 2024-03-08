package com.delivery_api.Delivery.API.Repository;

import com.delivery_api.Delivery.API.Entities.Wsef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WsefRepository extends JpaRepository<Wsef, Long> {
    Wsef findByBorders(String interval);
}
