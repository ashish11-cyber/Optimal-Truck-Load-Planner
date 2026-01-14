// src/main/java/com/example/loadoptimizer/dto/Truck.java
package com.example.loadoptimizer.dto;

import lombok.Data;

@Data
public class Truck {
    private String id;
    private long maxWeightLbs;
    private long maxVolumeCuft;
}