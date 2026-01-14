// src/main/java/com/example/loadoptimizer/dto/OptimizeRequest.java
package com.example.loadoptimizer.dto;

import lombok.Data;
import java.util.List;

@Data
public class OptimizeRequest {
    private Truck truck;
    private List<Order> orders;
}