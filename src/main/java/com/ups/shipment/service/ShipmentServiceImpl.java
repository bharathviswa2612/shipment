package com.ups.shipment.service;

import com.ups.shipment.dto.*;
import com.ups.shipment.entity.Shipment;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.exceptionhandling.DuplicateOrderIdException;
import com.ups.shipment.exceptionhandling.InvalidStatusTransitionException;
import com.ups.shipment.exceptionhandling.ShipmentNotFoundException;
import com.ups.shipment.repository.ShipmentRepository;

import com.ups.shipment.repository.projection.ShipmentAggregateProjection;
import com.ups.shipment.repository.projection.ShipmentSummaryProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request) {

        log.info("Creating shipment for orderId: {}", request.getOrderId());

        // Check if orderId already exists
        if (shipmentRepository.existsByOrderId(request.getOrderId())) {
            throw new DuplicateOrderIdException(request.getOrderId());
        }

        Shipment shipment = mapToEntity(request);

        shipment.setStatus(ShipmentStatus.CREATED);

        Shipment saved = shipmentRepository.save(shipment);

        log.info("Shipment created with id: {}", saved.getShipmentId());

        return mapToResponse(saved);
    }
    public ShipmentResponse getShipmentById(UUID shipmentId) {
        log.info("Fetching shipment with ID: {}", shipmentId);

        Shipment shipment = shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> {
                    log.error("Shipment not found for ID: {}", shipmentId);
                    return new ShipmentNotFoundException("Shipment not found");
                });

        log.debug("Shipment details retrieved: orderId={}, status={}, createdAt={}",
                shipment.getOrderId(), shipment.getStatus(), shipment.getCreatedAt());

        ShipmentResponse response = ShipmentResponse.builder()
                .shipmentId(shipment.getShipmentId())
                .orderId(shipment.getOrderId())
                .sourceAddress(shipment.getSourceAddress())
                .destinationAddress(shipment.getDestinationAddress())
                .weight(shipment.getWeight())
                .status(shipment.getStatus())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();

        log.info("Returning shipment response for ID: {}", shipmentId);
        return response;
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
    public UpdateStatusResponse updateStatus(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findByShipmentId(shipmentId)
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
                .status(shipment.getStatus())
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

    //UPS-041
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("weight", "createdAt");

    @Override
    public ShipmentSummaryResponse getShipmentSummary(ShipmentSummaryRequest request) {

        // 1️⃣ Parse & validate status
        ShipmentStatus status = null;
        if (request.getStatus() != null) {
            try {
                status = ShipmentStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value: " + request.getStatus());
            }
        }

        // 2️⃣ Validate weight range
        if (request.getMinWeight() != null && request.getMaxWeight() != null &&
                request.getMinWeight().compareTo(request.getMaxWeight()) > 0) {
            throw new IllegalArgumentException("Invalid weight range: minWeight > maxWeight");
        }

        // 3️⃣ Pagination defaults + validation
        int page = Optional.ofNullable(request.getPage()).orElse(0);
        int size = Optional.ofNullable(request.getSize()).orElse(10);

        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination values");
        }

        // 4️⃣ Sorting
        Sort sort = Sort.unsorted();
        if (request.getSortBy() != null) {

            if (!ALLOWED_SORT_FIELDS.contains(request.getSortBy())) {
                throw new IllegalArgumentException("Invalid sort field: " + request.getSortBy());
            }

            Sort.Direction direction =
                    "ASC".equalsIgnoreCase(Optional.ofNullable(request.getSortOrder()).orElse("ASC"))
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;

            sort = Sort.by(direction, request.getSortBy());
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        // 5️⃣ Fetch summary (projection)
        Page<ShipmentSummaryProjection> pageResult =
                shipmentRepository.findShipmentSummaries(
                        status,
                        request.getMinWeight(),
                        request.getMaxWeight(),
                        pageable
                );

        List<ShipmentSummaryResponse.ShipmentSummaryData> data = pageResult.getContent().stream()
                .map(p -> ShipmentSummaryResponse.ShipmentSummaryData.builder()
                        .shipmentId(p.getShipmentId())
                        .status(p.getStatus())
                        .weight(p.getWeight())
                        .build())
                .toList(); // Java 16+

        // 6️⃣ Aggregates (null-safe)
        ShipmentAggregateProjection aggregates =
                shipmentRepository.getShipmentAggregates(
                        status,
                        request.getMinWeight(),
                        request.getMaxWeight()
                );

        long delivered = 0, inTransit = 0, failed = 0;

        if (aggregates != null) {
            delivered = Optional.ofNullable(aggregates.getDeliveredCount()).orElse(0L);
            inTransit = Optional.ofNullable(aggregates.getInTransitCount()).orElse(0L);
            failed = Optional.ofNullable(aggregates.getFailedCount()).orElse(0L);
        }

        // 7️⃣ Response
        return ShipmentSummaryResponse.builder()
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .deliveredCount(delivered)
                .inTransitCount(inTransit)
                .failedCount(failed)
                .data(data)
                .build();
    }

    @Override
    public List<ShipmentResponse> getShipment() {
        // Fetch all shipments from DB
        List<Shipment> shipments = shipmentRepository.findAll();

        // Map each Shipment entity to ShipmentResponse DTO
        return shipments.stream()
                .map(shipment -> ShipmentResponse.builder()
                        .shipmentId(UUID.fromString(shipment.getShipmentId().toString()))
                        .orderId(shipment.getOrderId())
                        .sourceAddress(shipment.getSourceAddress())
                        .destinationAddress(shipment.getDestinationAddress())
                        .weight(shipment.getWeight())
                        .status(ShipmentStatus.valueOf(shipment.getStatus().name()))
                        .createdAt(shipment.getCreatedAt())
                        .updatedAt(shipment.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }



}