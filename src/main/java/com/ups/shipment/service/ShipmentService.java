package com.ups.shipment.service;

import com.ups.shipment.dto.*;
import com.ups.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface ShipmentService {
    ShipmentResponse createShipment(ShipmentRequest request);
    UpdateStatusResponse updateStatus(UUID shipmentId, @NotNull ShipmentStatus status);
    ShipmentResponse getShipmentById(UUID uuid);
    ShipmentSummaryResponse getShipmentSummary(ShipmentSummaryRequest request);
    List<ShipmentResponse> getShipment();
}
