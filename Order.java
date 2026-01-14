// src/main/java/com/example/loadoptimizer/dto/Order.java
package com.example.loadoptimizer.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Order {
    private String id;
    private long payoutCents;
    private long weightLbs;
    private long volumeCuft;
    private String origin;
    private String destination;
    private LocalDate pickupDate;
    private LocalDate deliveryDate;
    private boolean isHazmat;
}