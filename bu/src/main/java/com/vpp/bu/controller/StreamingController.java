package com.vpp.bu.controller;

import com.vpp.bu.dto.BatteryUpdate;
import com.vpp.bu.service.MessageListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream")
public class StreamingController {

    @Autowired
    MessageListenerService messageListenerService;

    @CrossOrigin
    @GetMapping(value = "/batteryUpdates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BatteryUpdate> streamBatteryUpdates() {
        return messageListenerService.getUpdatesStream();
    }
}
