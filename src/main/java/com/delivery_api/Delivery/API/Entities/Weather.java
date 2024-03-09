package com.delivery_api.Delivery.API.Entities;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import jakarta.persistence.GenerationType;

@Entity
@Table(name = "Weather")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public String station;
    public String WMO;
    public Double air;
    public Double wind;
    public String weather;
    public Timestamp observation;

    public Weather(String station, String WMO, Double air, Double wind, String weather, Timestamp observation) {
        this.station = station;
        this.WMO = WMO;
        this.air = air;
        this.wind = wind;
        this.weather = weather;
        this.observation = observation;
    }
}
