package com.vpp.cc.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpp.cc.dto.BatteryStatsResponse;
import com.vpp.cc.model.Battery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public ReactiveRedisTemplate<String, List<Battery>> reactiveRedisTemplateForList(
            ReactiveRedisConnectionFactory connectionFactory) {

        // Create an ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Use JavaType to specify the exact List<Battery> type
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, Battery.class);

        // Create the Jackson2JsonRedisSerializer with the specific JavaType
        Jackson2JsonRedisSerializer<List<Battery>> serializer = new Jackson2JsonRedisSerializer<>(javaType);

        RedisSerializationContext<String, List<Battery>> context = RedisSerializationContext
                .<String, List<Battery>>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Battery> reactiveRedisTemplateForBattery(
            ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Battery> serializer = new Jackson2JsonRedisSerializer<>(Battery.class);

        RedisSerializationContext<String, Battery> context = RedisSerializationContext
                .<String, Battery>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, BatteryStatsResponse> reactiveRedisTemplateForStats(
            ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<BatteryStatsResponse> serializer = new Jackson2JsonRedisSerializer<>(BatteryStatsResponse.class);

        RedisSerializationContext<String, BatteryStatsResponse> context = RedisSerializationContext
                .<String, BatteryStatsResponse>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}