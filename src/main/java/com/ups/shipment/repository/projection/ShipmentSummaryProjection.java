package com.ups.shipment.repository.projection;

import com.ups.shipment.entity.ShipmentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface ShipmentSummaryProjection {
    UUID getShipmentId();
    ShipmentStatus getStatus();
    BigDecimal getWeight();
}
