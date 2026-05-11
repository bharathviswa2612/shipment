package com.ups.shipment.controller;

import com.ups.shipment.dto.*;
import com.ups.shipment.service.ShipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Slf4j
@RestController
@Tag(name = "Shipment APIs")
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @ApiResponse(responseCode = "200", description = "Shipment found")
    @ApiResponse(responseCode = "404", description = "Shipment Id not found")
    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipment(@Parameter(description = "Shipment ID") @PathVariable String shipmentId) {
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

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getShipment() {

        List<ShipmentResponse> response = shipmentService.getShipment();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<UpdateStatusResponse> updateShipmentStatus(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody UpdateStatusRequest request) {

        UpdateStatusResponse response = shipmentService.updateStatus(shipmentId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all products")
    @GetMapping("/summary")
    public ResponseEntity<ShipmentSummaryResponse> getShipmentSummary(
            @Valid ShipmentSummaryRequest request) {
        return ResponseEntity.ok(shipmentService.getShipmentSummary(request));
    }

}