package com.ups.shipment.service;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;

public interface ShipmentService {
    ShipmentResponse createShipment(ShipmentRequest request);
}
