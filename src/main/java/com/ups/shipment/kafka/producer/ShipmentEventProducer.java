package com.ups.shipment.kafka.producer;

import com.ups.shipment.kafka.event.ShipmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventProducer {

    private final KafkaTemplate<String, ShipmentEvent> kafkaTemplate;

    public void publishShipmentEvent(ShipmentEvent event) {

        kafkaTemplate.send(
                "shipment-status-events",
                event.getShipmentId().toString(),
                event
        ).whenComplete((result, ex) -> {

            if (ex == null) {
                log.info("Event published successfully: {}", event);
            } else {
                log.error("Failed to publish event: {}", event, ex);
            }

        });
    }
}