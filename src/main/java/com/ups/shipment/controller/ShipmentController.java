package com.ups.shipment.controller;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.dto.UpdateStatusRequest;
import com.ups.shipment.dto.UpdateStatusResponse;
import com.ups.shipment.service.ShipmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {

        ShipmentResponse response = shipmentService.createShipment(request);

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<UpdateStatusResponse> updateShipmentStatus(
            @PathVariable Long shipmentId,
            @Valid @RequestBody UpdateStatusRequest request) {

        UpdateStatusResponse response = shipmentService.updateStatus(shipmentId, request.getStatus());
        return ResponseEntity.ok(response);
    }


}