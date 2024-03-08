package com.delivery_api.Delivery.API.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Rbf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rbf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public String city;
    public String vehicle;
    public double fee;

    public Rbf(String city, String vehicle, double fee) {
        this.city = city;
        this.vehicle = vehicle;
        this.fee = fee;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }
}
