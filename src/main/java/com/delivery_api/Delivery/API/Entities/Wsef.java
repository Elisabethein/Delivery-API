package com.delivery_api.Delivery.API.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Wsef")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wsef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public String borders;
    public double fee;

    public Wsef(String value, double fee) {
        this.borders = value;
        this.fee = fee;
    }
}
