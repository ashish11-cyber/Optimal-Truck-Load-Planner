// src/main/java/com/example/loadoptimizer/controller/LoadOptimizerController.java
package com.example.loadoptimizer.controller;

import com.example.loadoptimizer.dto.OptimizeRequest;
import com.example.loadoptimizer.dto.OptimizeResponse;
import com.example.loadoptimizer.service.LoadOptimizerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/load-optimizer")
public class LoadOptimizerController {

    private final LoadOptimizerService service;

    public LoadOptimizerController(LoadOptimizerService service) {
        this.service = service;
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizeResponse> optimize(@Valid @RequestBody OptimizeRequest request) {
        try {
            OptimizeResponse response = service.optimize(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    OptimizeResponse.builder()
                            .truckId(request.getTruck() != null ? request.getTruck().getId() : "unknown")
                            .selectedOrderIds(Collections.emptyList())
                            .totalPayoutCents(0)
                            .build()
            );
        }
    }
}