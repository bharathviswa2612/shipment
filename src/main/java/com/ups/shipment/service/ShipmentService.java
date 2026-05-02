package com.ups.shipment.service;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.dto.UpdateStatusResponse;
import com.ups.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public interface ShipmentService {
    ShipmentResponse createShipment(ShipmentRequest request);
    UpdateStatusResponse updateStatus(Long shipmentId, @NotNull ShipmentStatus status);
}
