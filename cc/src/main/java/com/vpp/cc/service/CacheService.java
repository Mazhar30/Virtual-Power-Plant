package com.vpp.cc.service;

import com.vpp.cc.dto.BatteryStatsResponse;
import com.vpp.cc.model.Battery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Value("${config.cache.time-to-live}")
    private int cacheTtl;

    private final ReactiveRedisTemplate<String, List<Battery>> redisTemplateForBatteryList;

    private final ReactiveRedisTemplate<String, Battery> redisTemplateForBattery;

    private final ReactiveRedisTemplate<String, BatteryStatsResponse> redisTemplateForStats;

    public CacheService(ReactiveRedisTemplate<String, List<Battery>> redisTemplateForBatteryList, ReactiveRedisTemplate<String, Battery> redisTemplateForBattery, ReactiveRedisTemplate<String, BatteryStatsResponse> redisTemplateForStats) {
        this.redisTemplateForBatteryList = redisTemplateForBatteryList;
        this.redisTemplateForBattery = redisTemplateForBattery;
        this.redisTemplateForStats = redisTemplateForStats;
    }

    private Duration getCacheTtl() {
        return Duration.ofMinutes(cacheTtl);
    }

    public Flux<Battery> getBatteriesFromCache(String key) {
        return redisTemplateForBatteryList.opsForValue()
                .get(key)
                .flatMapMany(batteries -> batteries != null ? Flux.fromIterable(batteries) : Flux.empty())
                .switchIfEmpty(
                        redisTemplateForBattery.opsForValue()
                                .get(key)
                                .flux()
                                .filter(Objects::nonNull)
                                .flatMap(Flux::just)
                );
    }

    public Mono<BatteryStatsResponse> getStatsFromCache(String key) {
        logger.info("Fetching stats from cache");
        return redisTemplateForStats.opsForValue().get(key);
    }

    public Mono<Boolean> saveStatsToCache(String key, BatteryStatsResponse batteryStatsResponse) {
        logger.info("Saving stats to cache");
        return redisTemplateForStats.opsForValue().set(key, batteryStatsResponse, getCacheTtl());
    }

    public Mono<Boolean> saveBatteriesToCache(String key, List<Battery> batteries) {
        return redisTemplateForBatteryList.opsForValue().set(key, batteries, getCacheTtl());
    }

    // Clear all battery caches
    public Mono<Boolean> clearAllBatteryCaches() {
        // Using scan and deleting based on a pattern like "batteries:*"
        return redisTemplateForBatteryList.scan(ScanOptions.scanOptions().match("batteries:*").build())
                .doOnNext(key -> logger.info("Found key: {}", key))  // Log each found key
                .flatMap(redisTemplateForBattery::delete) // Delete each matching key
                .collectList()
                .doOnTerminate(() -> logger.info("Cleared all battery-related cache entries"))
                .map(keys -> true) // Return true after clearing
                .onErrorResume(error -> {
                    logger.error("Failed to clear battery caches: {}", error.getMessage());
                    return Mono.just(false); // Return false on error
                });
    }
}
