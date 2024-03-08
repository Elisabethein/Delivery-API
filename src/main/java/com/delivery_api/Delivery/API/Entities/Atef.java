package com.delivery_api.Delivery.API.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Atef")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Atef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public String borders;
    public double fee;

    public Atef(String value, double fee) {
        this.borders = value;
        this.fee = fee;
    }
}
