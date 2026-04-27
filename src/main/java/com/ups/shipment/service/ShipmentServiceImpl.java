package com.ups.shipment.service;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.entity.Shipment;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.repository.ShipmentRepository;
import com.ups.shipment.service.ShipmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request) {

        log.info("Creating shipment for orderId: {}", request.getOrderId());

        Shipment shipment = mapToEntity(request);

        Shipment saved = shipmentRepository.save(shipment);

        log.info("Shipment created successfully with shipmentId: {}", saved.getShipmentId());

        return mapToResponse(saved);
    }

    private Shipment mapToEntity(ShipmentRequest request) {
        return Shipment.builder()
                .orderId(request.getOrderId())
                .sourceAddress(request.getSourceAddress())
                .destinationAddress(request.getDestinationAddress())
                .weight(request.getWeight())
                .status(ShipmentStatus.CREATED)
                .build();
    }

    private ShipmentResponse mapToResponse(Shipment shipment) {
        return ShipmentResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .orderId(shipment.getOrderId())
                .sourceAddress(shipment.getSourceAddress())
                .destinationAddress(shipment.getDestinationAddress())
                .weight(shipment.getWeight())
                .status(shipment.getStatus())
                .createdAt(shipment.getCreatedAt())
                .build();
    }
}