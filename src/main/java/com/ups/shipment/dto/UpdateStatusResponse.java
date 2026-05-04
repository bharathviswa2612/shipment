package com.ups.shipment.dto;

import com.ups.shipment.entity.ShipmentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusResponse {
    private UUID shipmentId;
    private ShipmentStatus status;
    private LocalDateTime updatedAt;
    private String message;
}
