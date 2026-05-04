package com.ups.shipment.controller;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.dto.UpdateStatusRequest;
import com.ups.shipment.dto.UpdateStatusResponse;
import com.ups.shipment.service.ShipmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable String shipmentId) {
        log.info("Received request to fetch shipment with ID: {}", shipmentId);

        // Let global exception handler manage invalid UUID or not found cases
        UUID uuid = UUID.fromString(shipmentId);
        ShipmentResponse response = shipmentService.getShipmentById(uuid);

        log.info("Successfully fetched shipment with ID: {}", shipmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request) {

        ShipmentResponse response = shipmentService.createShipment(request);

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<UpdateStatusResponse> updateShipmentStatus(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody UpdateStatusRequest request) {

        UpdateStatusResponse response = shipmentService.updateStatus(shipmentId, request.getStatus());
        return ResponseEntity.ok(response);
    }


}