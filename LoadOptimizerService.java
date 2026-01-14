// src/main/java/com/example/loadoptimizer/service/LoadOptimizerService.java
package com.example.loadoptimizer.service;

import com.example.loadoptimizer.dto.OptimizeRequest;
import com.example.loadoptimizer.dto.OptimizeResponse;
import com.example.loadoptimizer.dto.Order;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LoadOptimizerService {

    public OptimizeResponse optimize(OptimizeRequest request) {
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            return emptyResponse(request.getTruck().getId());
        }

        List<Order> orders = request.getOrders();
        int n = orders.size();
        if (n > 22) {
            throw new IllegalArgumentException("Too many orders (max 22 supported)");
        }

        Truck truck = request.getTruck();
        long maxW = truck.getMaxWeightLbs();
        long maxV = truck.getMaxVolumeCuft();

        // DP: dp[mask] = best payout achievable with that subset
        long[] dp = new long[1 << n];
        Arrays.fill(dp, -1);
        dp[0] = 0;

        // Also keep track of best weight & volume for reconstruction
        long[] bestWeight = new long[1 << n];
        long[] bestVolume = new long[1 << n];

        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] == -1) continue;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) continue;

                Order ord = orders.get(i);

                // Check compatibility with existing orders in mask
                boolean compatible = true;
                long newWeight = bestWeight[mask] + ord.getWeightLbs();
                long newVolume = bestVolume[mask] + ord.getVolumeCuft();

                if (newWeight > maxW || newVolume > maxV) {
                    compatible = false;
                }

                // All orders must share same origin → destination
                if (!isSameLane(orders, mask, ord)) {
                    compatible = false;
                }

                // Hazmat: if any hazmat already in mask, cannot add non-hazmat (simplified rule)
                // In real world often opposite — but let's assume "hazmat must go alone or with hazmat"
                if (ord.isHazmat() && hasNonHazmat(orders, mask)) {
                    compatible = false;
                }
                if (!ord.isHazmat() && hasHazmat(orders, mask)) {
                    compatible = false;
                }

                // Very simplified time-window check
                if (!timeWindowsCompatible(orders, mask, ord)) {
                    compatible = false;
                }

                if (!compatible) continue;

                int newMask = mask | (1 << i);
                long newPayout = dp[mask] + ord.getPayoutCents();

                if (newPayout > dp[newMask] || dp[newMask] == -1) {
                    dp[newMask] = newPayout;
                    bestWeight[newMask] = newWeight;
                    bestVolume[newMask] = newVolume;
                }
            }
        }

        // Find best mask
        long maxPayout = -1;
        int bestMask = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] > maxPayout) {
                maxPayout = dp[mask];
                bestMask = mask;
            }
        }

        if (maxPayout <= 0) {
            return emptyResponse(truck.getId());
        }

        // Reconstruct selected orders
        List<String> selected = new ArrayList<>();
        long tw = bestWeight[bestMask];
        long tv = bestVolume[bestMask];

        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                selected.add(orders.get(i).getId());
            }
        }

        double uw = maxW > 0 ? (tw * 100.0 / maxW) : 0;
        double uv = maxV > 0 ? (tv * 100.0 / maxV) : 0;

        return OptimizeResponse.builder()
                .truckId(truck.getId())
                .selectedOrderIds(selected)
                .totalPayoutCents(maxPayout)
                .totalWeightLbs(tw)
                .totalVolumeCuft(tv)
                .utilizationWeightPercent(Math.round(uw * 100) / 100.0)
                .utilizationVolumePercent(Math.round(uv * 100) / 100.0)
                .build();
    }

    private boolean isSameLane(List<Order> orders, int mask, Order candidate) {
        String orig = candidate.getOrigin();
        String dest = candidate.getDestination();

        for (int i = 0; i < orders.size(); i++) {
            if ((mask & (1 << i)) == 0) continue;
            Order o = orders.get(i);
            if (!o.getOrigin().equals(orig) || !o.getDestination().equals(dest)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasHazmat(List<Order> orders, int mask) {
        for (int i = 0; i < orders.size(); i++) {
            if ((mask & (1 << i)) != 0 && orders.get(i).isHazmat()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNonHazmat(List<Order> orders, int mask) {
        for (int i = 0; i < orders.size(); i++) {
            if ((mask & (1 << i)) != 0 && !orders.get(i).isHazmat()) {
                return true;
            }
        }
        return false;
    }

    private boolean timeWindowsCompatible(List<Order> orders, int mask, Order candidate) {
        // Very naive version — in real life you'd need earliest pickup / latest delivery logic
        LocalDate candPick = candidate.getPickupDate();
        LocalDate candDel = candidate.getDeliveryDate();

        for (int i = 0; i < orders.size(); i++) {
            if ((mask & (1 << i)) == 0) continue;
            Order o = orders.get(i);
            // Very loose check
            if (candPick.isAfter(o.getDeliveryDate()) || candDel.isBefore(o.getPickupDate())) {
                return false;
            }
        }
        return true;
    }

    private OptimizeResponse emptyResponse(String truckId) {
        return OptimizeResponse.builder()
                .truckId(truckId)
                .selectedOrderIds(Collections.emptyList())
                .totalPayoutCents(0)
                .totalWeightLbs(0)
                .totalVolumeCuft(0)
                .utilizationWeightPercent(0.0)
                .utilizationVolumePercent(0.0)
                .build();
    }
}