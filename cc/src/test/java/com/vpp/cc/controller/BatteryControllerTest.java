package com.vpp.cc.controller;

import com.vpp.cc.dto.BatteryResponse;
import com.vpp.cc.dto.BatteryStatsResponse;
import com.vpp.cc.model.Battery;
import com.vpp.cc.service.BatteryService;
import com.vpp.cc.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BatteryControllerTest {

    private WebTestClient webTestClient;
    private BatteryService batteryService;

    @BeforeEach
    void setUp() {
        batteryService = mock(BatteryService.class);
        MetricsService metricsService = mock(MetricsService.class);
        webTestClient = WebTestClient.bindToController(new BatteryController(batteryService, metricsService)).build();
    }

    @Test
    void getBatteriesByRange() {
        String start = "A1";
        String end = "A5";
        Long minCapacity = 100L;
        Long maxCapacity = 500L;

        Battery battery1 = new Battery("1", "Midland", "4501", 2400);
        Battery battery2 = new Battery("1", "Wetland", "4501", 2400);

        BatteryResponse batteryResponse = new BatteryResponse(List.of("Midland","Wetland"), 4800, 2400);

        when(batteryService.getBatteriesByPostcodeRangeAndCapacity(start, end, minCapacity, maxCapacity))
                .thenReturn(Flux.just(battery1, battery2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/batteries/getBatteries")
                        .queryParam("startPostcode", start)
                        .queryParam("endPostcode", end)
                        .queryParam("minCapacity", minCapacity)
                        .queryParam("maxCapacity", maxCapacity)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BatteryResponse.class).equals(batteryResponse);
    }

    @Test
    void getBatteriesBelowCapacity() {
        Long maxCapacity = 5000L;

        Battery battery1 = new Battery("1", "Midland", "4501", 2400);
        Battery battery2 = new Battery("2", "Wetland", "4501", 2400);

        when(batteryService.getBatteriesBelowCapacityThreshold(maxCapacity))
                .thenReturn(Flux.just(battery1, battery2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/batteries/getBatteriesBelowCapacity")
                        .queryParam("capacity", maxCapacity)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Battery.class).contains(battery1, battery2);
    }

    @Test
    void getBatteryStats() {
        BatteryStatsResponse batteryStatsResponse = new BatteryStatsResponse(2, 4800, 2400);

        when(batteryService.getAllBatteriesStat()).thenReturn(Mono.just(batteryStatsResponse));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/batteries/stats")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(BatteryStatsResponse.class).equals(batteryStatsResponse);
    }

//    @Test
//    void save() {
//        Battery battery1 = new Battery("1", "Midland", "4501", 2400);
//        Battery battery2 = new Battery("2", "Wetland", "4501", 2400);
//
//        when(batteryService.saveBatteries(Flux.just(battery1, battery2))).thenReturn(Flux.just(battery1, battery2));
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/batteries/save")
//                        .build())
//                .body(Flux.just(battery1, battery2), Battery.class)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(String.class)
//                .isEqualTo("Batteries saved successfully");
//    }
}
