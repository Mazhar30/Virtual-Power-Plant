package com.vpp.cc.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final Map<String, Counter> endpointCounters = new ConcurrentHashMap<>();

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Gauge to monitor active requests
        meterRegistry.gauge("api.active.requests", activeRequests);
    }

    public void trackRequestStart(String endpoint) {
        activeRequests.incrementAndGet();

        // Increment the specific endpoint counter
        endpointCounters.computeIfAbsent(endpoint, key ->
                meterRegistry.counter("api.calls.total", "endpoint", key)
        ).increment();
    }

    public void trackRequestEnd() {
        activeRequests.decrementAndGet();
    }
}
