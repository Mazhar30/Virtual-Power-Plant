package com.vpp.cc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingConfig {

    private Map<String, Integer> endpoints;

    public Map<String, Integer> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, Integer> endpoints) {
        this.endpoints = endpoints;
    }
}