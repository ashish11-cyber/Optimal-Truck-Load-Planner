// src/main/java/com/example/loadoptimizer/dto/OptimizeResponse.java
package com.example.loadoptimizer.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OptimizeResponse {
    private String truckId;
    private List<String> selectedOrderIds;
    private long totalPayoutCents;
    private long totalWeightLbs;
    private long totalVolumeCuft;
    private double utilizationWeightPercent;
    private double utilizationVolumePercent;
}