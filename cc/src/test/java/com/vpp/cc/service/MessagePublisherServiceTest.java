package com.vpp.cc.service;

import com.vpp.cc.dto.BatteryUpdate;
import com.vpp.cc.repository.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagePublisherServiceTest {

    private RabbitTemplate rabbitTemplate;
    private MessagePublisherService messagePublisherService;
    private BatteryRepository batteryRepository;

    @BeforeEach
    void setUp() {
        batteryRepository = mock(BatteryRepository.class);
        rabbitTemplate = mock(RabbitTemplate.class);
        messagePublisherService = new MessagePublisherService(rabbitTemplate, batteryRepository);
    }

    @Test
    void sendCapacityUpdate() {
        // Given
        String queueName = "testQueue";
        String batteryName = "Battery1";
        int capacity = 100;

        ReflectionTestUtils.setField(messagePublisherService, "batteryQueue", queueName);

        // When
        messagePublisherService.sendCapacityUpdate(batteryName, capacity);

        // Then
        ArgumentCaptor<BatteryUpdate> captor = forClass(BatteryUpdate.class);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(queueName), captor.capture());

        BatteryUpdate expectedUpdate = new BatteryUpdate(batteryName, capacity);
        BatteryUpdate actualUpdate = captor.getValue();

        assertEquals(expectedUpdate, actualUpdate);
    }
}