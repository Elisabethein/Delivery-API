package com.delivery_api.Delivery.API.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Wpef")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wpef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public String containing;
    public double fee;

    public Wpef(String containing, double fee) {
        this.containing = containing;
        this.fee = fee;
    }
}

