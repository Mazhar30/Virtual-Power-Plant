package com.vpp.cc.service;

import com.vpp.cc.dto.BatteryUpdate;
import com.vpp.cc.repository.BatteryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessagePublisherService {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisherService.class);

    @Value("${config.message.queue.name}")
    private String batteryQueue;

    private final RabbitTemplate rabbitTemplate;

    private final BatteryRepository batteryRepository;

    public MessagePublisherService(RabbitTemplate rabbitTemplate, BatteryRepository batteryRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.batteryRepository = batteryRepository;
    }

    public void sendCapacityUpdate(String batteryName, int capacity) {
        BatteryUpdate update = new BatteryUpdate(batteryName, capacity);
        rabbitTemplate.convertAndSend(batteryQueue, update);
    }

    @Scheduled(fixedRateString = "${config.message.publishing.scheduler.frequency}")
    public void simulateUpdate() {
        logger.info("Scheduler fetching all battery info from db");
        batteryRepository.findAll()
                .flatMap(battery -> {
                    try {
                        sendCapacityUpdate(battery.getName(), battery.getCapacity());
                        return Mono.just(battery); // Return a Mono to continue processing
                    } catch (Exception e) {
                        logger.error("Error processing battery [{}]", battery.getName(), e);
                        return Mono.empty();
                    }
                })
                .collectList()
                .doOnError(e -> logger.error("Error occurred while processing batteries", e))
                .subscribe(batteries -> logger.info("Successfully processed {} batteries", batteries.size()));
    }
}