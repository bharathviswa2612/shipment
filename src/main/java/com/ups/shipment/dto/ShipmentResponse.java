package com.ups.shipment.dto;

import com.ups.shipment.entity.ShipmentStatus;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponse {

    private Long shipmentId;
    private String orderId;
    private String sourceAddress;
    private String destinationAddress;
    private BigDecimal weight;
    private ShipmentStatus status;
    private LocalDateTime createdAt;
}