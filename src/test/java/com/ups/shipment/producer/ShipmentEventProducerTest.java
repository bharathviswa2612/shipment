package com.ups.shipment.producer;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.kafka.event.ShipmentEvent;
import com.ups.shipment.kafka.producer.ShipmentEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentEventProducerTest {

    @Mock
    private KafkaTemplate<String, ShipmentEvent> kafkaTemplate;

    @InjectMocks
    private ShipmentEventProducer shipmentEventProducer;

    @Test
    void testPublishShipmentEvent_success() {
        ShipmentEvent event = ShipmentEvent.builder()
                .eventId(UUID.randomUUID())
                .shipmentId(UUID.randomUUID())
                .status(ShipmentStatus.CREATED)
                .timestamp(LocalDateTime.now())
                .source("shipment-service")
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), any(ShipmentEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        shipmentEventProducer.publishShipmentEvent(event);

        verify(kafkaTemplate, times(1))
                .send("shipment-status-events", event.getShipmentId().toString(), event);
    }
}
