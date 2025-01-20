package com.vpp.cc.security;

import com.vpp.cc.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.Bandwidth;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements WebFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private final Map<String, Integer> endpointLimits;

    public RateLimitingFilter(RateLimitingConfig config) {
        this.endpointLimits = config.getEndpoints();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        // Get the endpoint path and rate limit
        String path = exchange.getRequest().getURI().getPath();
        path = path.replaceAll("/", ""); // Remove the slash
        int rateLimit = endpointLimits.getOrDefault(path, 50); // Default if endpoint not in config

        // Create or retrieve the bucket for this client and endpoint
        String key = clientIp + "::" + path; // Use client IP and path as the cache key
        Bucket bucket = cache.computeIfAbsent(key, k -> newBucket(rateLimit));

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange); // Proceed if the request is within the limit
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS); // HTTP 429 Too Many Requests
            return exchange.getResponse().setComplete();
        }
    }

    private Bucket newBucket(int rateLimit) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(rateLimit, Refill.intervally(rateLimit, Duration.ofMinutes(1))))
                .build();
    }
}

