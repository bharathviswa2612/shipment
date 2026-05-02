package com.ups.shipment.service;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.dto.UpdateStatusResponse;
import com.ups.shipment.entity.Shipment;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.exceptionhandling.InvalidStatusTransitionException;
import com.ups.shipment.exceptionhandling.ShipmentNotFoundException;
import com.ups.shipment.repository.ShipmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        shipment.setStatus(ShipmentStatus.CREATED);

        Shipment saved = shipmentRepository.save(shipment);

        log.info("Shipment created with id: {}", saved.getShipmentId());

        return mapToResponse(saved);
    }

    private Shipment mapToEntity(ShipmentRequest request) {

        return Shipment.builder()
                .orderId(request.getOrderId())
                .sourceAddress(request.getSourceAddress())
                .destinationAddress(request.getDestinationAddress())
                .weight(request.getWeight())
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

    @Override
    public UpdateStatusResponse updateStatus(Long shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with id " + shipmentId));

        ShipmentStatus currentStatus = shipment.getStatus();

        // Same status check
        if (currentStatus == newStatus) {
            throw new InvalidStatusTransitionException("Shipment is already in status " + newStatus);
        }

        // Validate transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        // Update status and timestamp
        shipment.setStatus(newStatus);
        shipment.setUpdatedAt(LocalDateTime.now());

        shipmentRepository.save(shipment);

        return UpdateStatusResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .status(newStatus)
                .updatedAt(shipment.getUpdatedAt())
                .message("Shipment status updated successfully")
                .build();
    }

    private boolean isValidTransition(ShipmentStatus current, ShipmentStatus next) {
        return switch (current) {
            case CREATED -> next == ShipmentStatus.PICKED || next == ShipmentStatus.CANCELLED;
            case PICKED -> next == ShipmentStatus.IN_TRANSIT || next == ShipmentStatus.CANCELLED;
            case IN_TRANSIT -> next == ShipmentStatus.DELIVERED || next == ShipmentStatus.CANCELLED;
            case DELIVERED -> next == ShipmentStatus.CANCELLED; // only cancel allowed
            case CANCELLED -> false; // cannot transition further
        };
    }
}