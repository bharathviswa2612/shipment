package com.ups.shipment.controller;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.service.ShipmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody ShipmentRequest request) {

        log.info("Received request to create shipment for orderId: {}", request.getOrderId());

        ShipmentResponse response = shipmentService.createShipment(request);

        log.info("Shipment created successfully with shipmentId: {}", response.getShipmentId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}