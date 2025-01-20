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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

@RestController
@RequestMapping("/api/batteries")
@Api(value = "Battery Management System", tags = "Battery API")
public class BatteryController {

    private static final Logger logger = LoggerFactory.getLogger(BatteryController.class);

    private final BatteryService batteryService;

    private final MetricsService metricsService;

    public BatteryController(BatteryService batteryService, MetricsService metricsService) {
        this.batteryService = batteryService;
        this.metricsService = metricsService;
    }

    @ApiOperation(value = "Add Batteries", notes = "Saves a list of batteries to the database.")
    @PostMapping("/save")
    public Mono<ResponseEntity<String>> addBatteries(@ApiParam(value = "List of Battery objects to save", required = true)
                                                         @RequestBody Flux<Battery> batteries) {
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

    @ApiOperation(value = "Get Batteries by Range", notes = "Fetch batteries within a specific postcode range.")
    @GetMapping("/getBatteries")
    public Mono<BatteryResponse> getBatteriesByRange(@ApiParam(value = "Start postcode", required = true)
                                                         @RequestParam String startPostcode,
                                                     @ApiParam(value = "End postcode", required = true)
                                                     @RequestParam String endPostcode,
                                                     @ApiParam(value = "Min Capacity", required = false)
                                                     @RequestParam(required = false) Long minCapacity,
                                                     @ApiParam(value = "Max Capacity", required = false)
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

    @ApiOperation(value = "Get Batteries Below Capacity Range", notes = "Fetch batteries below a specific cpacity threshold.")
    @GetMapping("/getBatteriesBelowCapacity")
    public Flux<Battery> getBatteriesBelowCapacity(@ApiParam(value = "Capacity Threshold", required = true)
                                                       @RequestParam Long capacity) {
        metricsService.trackRequestStart("/api/batteries/getBatteriesBelowCapacity");
        logger.info("Received request to fetch batteries below capacity threshold [{}]", capacity);
        return batteryService.getBatteriesBelowCapacityThreshold(capacity)
                .doOnComplete(metricsService::trackRequestEnd)
                .doOnError(error -> {
                    metricsService.trackRequestEnd();
                    logger.error("Error fetching batteries below capacity threshold: {}", error.getMessage());
                });
    }

    @ApiOperation(value = "Get Aggregate Battery Stats", notes = "Fetch and calculate all the battery's total and average capacity.")
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