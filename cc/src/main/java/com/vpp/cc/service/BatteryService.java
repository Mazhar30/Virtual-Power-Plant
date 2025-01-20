package com.vpp.cc.service;

import com.vpp.cc.dto.BatteryStatsResponse;
import com.vpp.cc.repository.BatteryRepository;
import com.vpp.cc.model.Battery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BatteryService {

    private static final Logger logger = LoggerFactory.getLogger(BatteryService.class);

    private final BatteryRepository batteryRepository;
    private final CacheService cacheService;

    public BatteryService(BatteryRepository batteryRepository, CacheService cacheService) {
        this.batteryRepository = batteryRepository;
        this.cacheService = cacheService;
    }

    public Flux<Battery> saveBatteries(Flux<Battery> batteries) {
        return batteries
                .flatMap(battery -> {
                    logger.info("Saving battery: {}", battery);
                    return batteryRepository.save(battery);
                }, 10) // Controlled concurrency: 10 threads
                .doOnComplete(() -> {
                    logger.info("All batteries saved successfully");
                    cacheService.clearAllBatteryCaches()
                            .doOnTerminate(() -> logger.info("Cache clearing completed"))
                            .subscribe();
                })
                .doOnError(error -> logger.error("Error saving batteries: {}", error.getMessage()));
    }

    public Flux<Battery> getBatteriesByPostcodeRangeAndCapacity(String start, String end,
                                                                Long minCapacity, Long maxCapacity) {
        logger.info("Fetching batteries in postcode range: {} - {} with capacity between {} and {}",
                start, end, minCapacity, maxCapacity);

        if (minCapacity == null) minCapacity = 0L;
        if (maxCapacity == null) maxCapacity = Long.MAX_VALUE;

        // Generate a unique cache key based on query parameters
        String cacheKey = String.format("batteries:%s:%s:%d:%d", start, end, minCapacity, maxCapacity);

        // Check the cache for existing data
        return cacheService.getBatteriesFromCache(cacheKey)
                .switchIfEmpty(
                        // If cache miss, query the database
                        batteryRepository.findByPostcodeRangeAndCapacity(start, end, minCapacity, maxCapacity)
                                .collectList() // Collect database results into a List
                                .flatMapMany(batteries -> {
                                    if (!batteries.isEmpty()) {
                                        // Save to cache and return the data
                                        return cacheService.saveBatteriesToCache(cacheKey, batteries)
                                                .thenMany(Flux.fromIterable(batteries));
                                    } else {
                                        return Flux.empty(); // No data to cache
                                    }
                                })
                );
    }

    public Flux<Battery> getBatteriesBelowCapacityThreshold(Long capacity) {
        logger.info("Fetching batteries with capacity below [{}]", capacity);
        if (capacity == null) capacity = Long.MAX_VALUE;

        // Generate a unique cache key
        String cacheKey = String.format("batteries:belowCapacity:%d", capacity);

        // Check the cache for existing data
        return cacheService.getBatteriesFromCache(cacheKey)
                .switchIfEmpty(
                        // If cache miss, query the database
                        batteryRepository.findByCapacity(capacity)
                                .collectList() // Collect database results into a List
                                .flatMapMany(batteries -> {
                                    if (!batteries.isEmpty()) {
                                        // Save to cache and return the data
                                        return cacheService.saveBatteriesToCache(cacheKey, batteries)
                                                .thenMany(Flux.fromIterable(batteries));
                                    } else {
                                        return Flux.empty(); // No data to cache
                                    }
                                })
                );
    }

    public Mono<BatteryStatsResponse> getAllBatteriesStat() {
        String cacheKey = "batteries:stats"; // Unique cache key for battery stats

        return cacheService.getStatsFromCache(cacheKey)
                .switchIfEmpty(
                        batteryRepository.findAll()
                                .collectList()
                                .map(batteries -> {
                                    int totalCapacity = batteries.stream().mapToInt(Battery::getCapacity).sum();
                                    int totalBatteries = batteries.size();
                                    double averageCapacity = totalBatteries == 0 ? 0 : (double) totalCapacity / totalBatteries;

                                    logger.info("Total Batteries: {}, Total Capacity: {}, Average Capacity: {}",
                                            totalBatteries, totalCapacity, averageCapacity);

                                    return new BatteryStatsResponse(totalBatteries, totalCapacity, averageCapacity);
                                })
                                .flatMap(stats -> {
                                    // Save stats to cache and return
                                    return cacheService.saveStatsToCache(cacheKey, stats)
                                            .thenReturn(stats);
                                })
                )
                .doOnError(error -> {
                    logger.error("Error calculating battery stats: {}", error.getMessage());
                });
    }
}