package com.vpp.bu.service;

import com.vpp.bu.config.RabbitMQConfig;
import com.vpp.bu.dto.BatteryUpdate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class MessageListenerService {

    private final Sinks.Many<BatteryUpdate> sink = Sinks.many().multicast().onBackpressureBuffer();

    @RabbitListener(queues = RabbitMQConfig.BATTERY_QUEUE)
    public void receiveBatteryUpdate(BatteryUpdate update) {
        sink.tryEmitNext(update);
    }

    public Flux<BatteryUpdate> getUpdatesStream() {
        return sink.asFlux();
    }
}
