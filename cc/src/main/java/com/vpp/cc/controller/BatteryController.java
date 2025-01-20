package com.vpp.cc.controller;

import com.vpp.cc.dto.BatteryResponse;
import com.vpp.cc.dto.BatteryStatsResponse;
import com.vpp.cc.model.Battery;
import com.vpp.cc.service.BatteryService;
import com.vpp.cc.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/batteries")
public class BatteryController {

    private static final Logger logger = LoggerFactory.getLogger(BatteryController.class);

    private final BatteryService batteryService;

    private final MetricsService metricsService;

    public BatteryController(BatteryService batteryService, MetricsService metricsService) {
        this.batteryService = batteryService;
        this.metricsService = metricsService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<String>> addBatteries(@RequestBody Flux<Battery> batteries) {
        metricsService.trackRequestStart("/api/batteries/save");
        return batteries
                .limitRate(1000) // Process up to 1000 items per request
                .collectList()
                .flatMap(list -> {
                    logger.info("Received {} batteries to save", list.size());
                    return batteryService.saveBatteries(Flux.fromIterable(list))
                            .then(Mono.fromCallable(() -> {
                                metricsService.trackRequestEnd();
                                return ResponseEntity.ok("Batteries saved successfully");
                            }));
                })
                .onErrorResume(error -> {
                    metricsService.trackRequestEnd();
                    logger.error("Error adding batteries: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to save batteries: " + error.getMessage()));
                });
    }

    @GetMapping("/getBatteries")
    public Mono<BatteryResponse> getBatteriesByRange(@RequestParam String startPostcode,
                                                     @RequestParam String endPostcode,
                                                     @RequestParam(required = false) Long minCapacity,
                                                     @RequestParam(required = false) Long maxCapacity) {

        metricsService.trackRequestStart("/api/batteries/getBatteries");
        logger.info("Received request to fetch batteries in postcode range: {} - {}", startPostcode, endPostcode);

        return batteryService.getBatteriesByPostcodeRangeAndCapacity(startPostcode, endPostcode, minCapacity, maxCapacity)
                .collectList()
                .map(batteries -> {
                    logger.info("Found {} batteries in the specified range", batteries.size());
                    List<String> batteryNames = batteries.stream()
                            .map(Battery::getName)
                            .sorted()
                            .toList();
                    int totalCapacity = batteries.stream().mapToInt(Battery::getCapacity).sum();
                    double averageCapacity = batteries.isEmpty() ? 0 : totalCapacity / (double) batteries.size();
                    metricsService.trackRequestEnd();
                    return new BatteryResponse(batteryNames, totalCapacity, averageCapacity);
                })
                .doOnError(error -> {
                    metricsService.trackRequestEnd();
                    logger.error("Error fetching batteries: ", error);
                });
    }

    @GetMapping("/getBatteriesBelowCapacity")
    public Flux<Battery> getBatteriesBelowCapacity(@RequestParam Long capacity) {
        metricsService.trackRequestStart("/api/batteries/getBatteriesBelowCapacity");
        logger.info("Received request to fetch batteries below capacity threshold [{}]", capacity);
        return batteryService.getBatteriesBelowCapacityThreshold(capacity)
                .doOnComplete(metricsService::trackRequestEnd)
                .doOnError(error -> {
                    metricsService.trackRequestEnd();
                    logger.error("Error fetching batteries below capacity threshold: {}", error.getMessage());
                });
    }

    @GetMapping("/stats")
    public Mono<BatteryStatsResponse> getBatteryStats() {
        metricsService.trackRequestStart("/api/batteries/stats");
        logger.info("Fetching real-time aggregated battery stats");
        return batteryService.getAllBatteriesStat()
                .doOnTerminate(metricsService::trackRequestEnd)
                .doOnError(error -> {
                    logger.error("Error fetching battery stats: {}", error.getMessage());
                });
    }
}