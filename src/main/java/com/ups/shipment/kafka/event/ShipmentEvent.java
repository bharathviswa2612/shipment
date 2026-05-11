package com.ups.shipment.kafka.event;

import com.ups.shipment.entity.ShipmentStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentEvent {
    private UUID eventId;
    private UUID shipmentId;
    private ShipmentStatus status;
    private LocalDateTime timestamp;
    private String source;
}
