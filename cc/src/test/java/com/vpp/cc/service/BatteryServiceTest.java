package com.vpp.cc.service;

import com.vpp.cc.dto.BatteryStatsResponse;
import com.vpp.cc.model.Battery;
import com.vpp.cc.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BatteryServiceTest {

    private BatteryService batteryService;
    private BatteryRepository batteryRepository;
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        batteryRepository = mock(BatteryRepository.class);
        cacheService = mock(CacheService.class);
        batteryService = new BatteryService(batteryRepository, cacheService);
    }

    @Test
    void getBatteriesByPostcodeRangeAndCapacity() {

        String start = "A1";
        String end = "A5";
        Long minCapacity = 100L;
        Long maxCapacity = 500L;

        Battery battery = new Battery("1", "Sweta", "4501", 2400);

        // Mock cache hit scenario
        when(cacheService.getBatteriesFromCache(start)).thenReturn(Flux.just(battery));
        // Mock cache miss scenario
        when(cacheService.getBatteriesFromCache(anyString())).thenReturn(Flux.empty());

        when(batteryRepository.findByPostcodeRangeAndCapacity(start, end, minCapacity, maxCapacity)).thenReturn(Flux.just(battery));

        // Mock saving to cache
        when(cacheService.saveBatteriesToCache(anyString(), ArgumentMatchers.eq(List.of(battery))))
                .thenReturn(Mono.just(true));
        // When
        Flux<Battery> result = batteryService.getBatteriesByPostcodeRangeAndCapacity(start, end, minCapacity, maxCapacity);

        // Then
        StepVerifier.create(result)
                .expectNext(battery)
                .verifyComplete();
    }

    @Test
    void getBatteriesBelowCapacityThreshold() {

        String start = "A1";
        Long maxCapacity = 500L;

        Battery battery = new Battery("1", "Sweta", "4501", 2400);

        // Mock cache hit scenario
        when(cacheService.getBatteriesFromCache(start)).thenReturn(Flux.just(battery));

        // Mock cache miss scenario
        when(cacheService.getBatteriesFromCache(anyString())).thenReturn(Flux.empty());

        when(batteryRepository.findByCapacity(maxCapacity)).thenReturn(Flux.just(battery));

        // Mock saving to cache
        when(cacheService.saveBatteriesToCache(anyString(), ArgumentMatchers.eq(List.of(battery))))
                .thenReturn(Mono.just(true));
        // When
        Flux<Battery> result = batteryService.getBatteriesBelowCapacityThreshold(maxCapacity);

        // Then
        StepVerifier.create(result)
                .expectNext(battery)
                .verifyComplete();
    }

//    @Test
//    void getAllBatteriesStat() {
//
//        String start = "A1";
//        BatteryStatsResponse batteryStatsResponse = new BatteryStatsResponse(10, 20000, 2400.0);
//
//        Battery battery = new Battery("1", "Midline", "4501", 2400);
//
//        // Mock cache hit scenario
//        when(cacheService.getStatsFromCache(start)).thenReturn(Mono.just(batteryStatsResponse));
//        // Mock cache miss scenario
//        when(cacheService.getStatsFromCache(anyString())).thenReturn(Mono.empty());
//
//        when(batteryRepository.findAll()).thenReturn(Flux.just(battery));
//
//        // Mock saving to cache
//        when(cacheService.saveStatsToCache(anyString(), ArgumentMatchers.eq(batteryStatsResponse)))
//                .thenReturn(Mono.just(true));
//
//        // When
//        Mono<BatteryStatsResponse> result = batteryService.getAllBatteriesStat();
//
//        // Then
//        StepVerifier.create(result)
//                .expectNext(batteryStatsResponse)
//                .verifyComplete();
//    }

    @Test
    void saveBatteries() {
        Battery battery = new Battery("1", "Midline", "4501", 2400);
        when(batteryRepository.save(battery)).thenReturn(Mono.just(battery));

        when(cacheService.clearAllBatteryCaches()).thenReturn(Mono.just(true));

        Flux<Battery> result = batteryService.saveBatteries(Flux.just(battery));

        StepVerifier.create(result)
                .expectNext(battery)
                .verifyComplete();
    }
}