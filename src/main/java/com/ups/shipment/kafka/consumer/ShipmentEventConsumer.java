package com.ups.shipment.kafka.consumer;

import com.ups.shipment.kafka.event.ShipmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShipmentEventConsumer {

    @KafkaListener(topics = "shipment-status-events", groupId = "shipment-service-group")
    public void consumeShipmentEvent(ShipmentEvent event) {
        log.info("Received shipment event: {}", event);

        // Example: trigger downstream logic
        // e.g., send notification, update tracking system, etc.
    }
}