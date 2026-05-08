
package com.ups.shipment.service;

import com.ups.shipment.dto.*;
import com.ups.shipment.entity.Shipment;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.exceptionhandling.InvalidStatusTransitionException;
import com.ups.shipment.exceptionhandling.ShipmentNotFoundException;
import com.ups.shipment.repository.ShipmentRepository;

import com.ups.shipment.repository.projection.ShipmentAggregateProjection;
import com.ups.shipment.repository.projection.ShipmentSummaryProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    @Test
    void createShipment_shouldCreateAndReturnShipmentResponse_whenValidRequest() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();

        ShipmentRequest request = ShipmentRequest.builder()
                .orderId("ORD123")
                .sourceAddress("Chennai")
                .destinationAddress("Bangalore")
                .weight(BigDecimal.valueOf(12.5))
                .build();

        Shipment savedShipment = Shipment.builder()
                .shipmentId(shipmentId)
                .orderId(request.getOrderId())
                .sourceAddress(request.getSourceAddress())
                .destinationAddress(request.getDestinationAddress())
                .weight(request.getWeight())
                .status(ShipmentStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);

        // Act
        ShipmentResponse response = shipmentService.createShipment(request);

        // Assert (Response validation)
        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals(request.getSourceAddress(), response.getSourceAddress());
        assertEquals(request.getDestinationAddress(), response.getDestinationAddress());
        assertEquals(request.getWeight(), response.getWeight());
        assertEquals(ShipmentStatus.CREATED, response.getStatus());
        assertNotNull(response.getCreatedAt());

        // Assert (Repository interaction)
        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository, times(1)).save(captor.capture());

        Shipment captured = captor.getValue();

        assertEquals(request.getOrderId(), captured.getOrderId());
        assertEquals(request.getSourceAddress(), captured.getSourceAddress());
        assertEquals(request.getDestinationAddress(), captured.getDestinationAddress());
        assertEquals(request.getWeight(), captured.getWeight());
        assertEquals(ShipmentStatus.CREATED, captured.getStatus());
    }
    @Test
    void updateStatus_successfulTransition() {

        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .status(ShipmentStatus.CREATED)
                .build();

        when(shipmentRepository.findByShipmentId(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        UpdateStatusResponse response = shipmentService.updateStatus(shipmentId, ShipmentStatus.PICKED);

        assertEquals(ShipmentStatus.PICKED, response.getStatus());
        assertEquals(shipmentId, response.getShipmentId());
        assertNotNull(response.getUpdatedAt());
        assertEquals("Shipment status updated successfully", response.getMessage());
    }

    @Test
    void updateStatus_shipmentNotFound() {
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findByShipmentId(shipmentId)).thenReturn(Optional.empty());

        assertThrows(ShipmentNotFoundException.class,
                () -> shipmentService.updateStatus(shipmentId, ShipmentStatus.PICKED));
    }

    @Test
    void updateStatus_invalidTransition() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .status(ShipmentStatus.DELIVERED)
                .build();

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        assertThrows(InvalidStatusTransitionException.class,
                () -> shipmentService.updateStatus(shipmentId, ShipmentStatus.IN_TRANSIT));
    }

    @Test
    void updateStatus_sameStatusConflict() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = Shipment.builder()
                .shipmentId(shipmentId)
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findByShipmentId(shipmentId)).thenReturn(Optional.of(shipment));

        assertThrows(InvalidStatusTransitionException.class,
                () -> shipmentService.updateStatus(shipmentId, ShipmentStatus.IN_TRANSIT));
    }

    @Test
    @DisplayName("Should return ShipmentResponse when shipment exists")
    void testGetShipmentById_Success() {
        UUID shipmentId = UUID.randomUUID();
        Shipment savedshipment = Shipment.builder()
                .shipmentId(shipmentId)
                .orderId("ORD123")
                .sourceAddress("Chennai")
                .destinationAddress("Bangalore")
                .weight(BigDecimal.valueOf(12.5))
                .status(ShipmentStatus.IN_TRANSIT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(shipmentRepository.findByShipmentId(shipmentId)).thenReturn(Optional.of(savedshipment));

        ShipmentResponse response = shipmentService.getShipmentById(shipmentId);

        assertNotNull(response);
        assertEquals(shipmentId, response.getShipmentId());
        assertEquals(savedshipment.getOrderId(), response.getOrderId());
        assertEquals(savedshipment.getStatus(), response.getStatus());
        verify(shipmentRepository, times(1)).findByShipmentId(shipmentId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when shipment not found")
    void testGetShipmentById_NotFound() {
        // Arrange
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findByShipmentId(shipmentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> shipmentService.getShipmentById(shipmentId),
                "Expected RuntimeException when shipment not found");

        assertEquals("Shipment not found", exception.getMessage(), "Exception message should match");

        // Verify repository interaction
        verify(shipmentRepository, times(1)).findByShipmentId(shipmentId);
    }

    // Helper projection stub
    private ShipmentSummaryProjection mockProjection(UUID id, ShipmentStatus status, BigDecimal weight) {
        return new ShipmentSummaryProjection() {
            @Override public UUID getShipmentId() { return id; }
            @Override public ShipmentStatus getStatus() { return status; }
            @Override public BigDecimal getWeight() { return weight; }
        };
    }

    private ShipmentAggregateProjection mockAggregate(long delivered, long inTransit, long failed) {
        return new ShipmentAggregateProjection() {
            @Override public Long getTotal() { return delivered + inTransit + failed; }
            @Override public Long getDeliveredCount() { return delivered; }
            @Override public Long getInTransitCount() { return inTransit; }
            @Override public Long getFailedCount() { return failed; }
        };
    }

    @Test
    void testValidSummaryRequest() {
        // Arrange
        UUID id = UUID.randomUUID();
        ShipmentSummaryRequest request = ShipmentSummaryRequest.builder()
                .status("DELIVERED")
                .page(0)
                .size(10)
                .build();

        Page<ShipmentSummaryProjection> page = new PageImpl<>(
                List.of(mockProjection(id, ShipmentStatus.DELIVERED, BigDecimal.valueOf(12.5)))
        );

        when(shipmentRepository.findShipmentSummaries(eq(ShipmentStatus.DELIVERED), any(), any(), any()))
                .thenReturn(page);
        when(shipmentRepository.getShipmentAggregates(eq(ShipmentStatus.DELIVERED), any(), any()))
                .thenReturn(mockAggregate(1, 0, 0));

        // Act
        ShipmentSummaryResponse response = shipmentService.getShipmentSummary(request);

        // Assert
        assertEquals(1, response.getDeliveredCount());
        assertEquals(0, response.getInTransitCount());
        assertEquals(0, response.getFailedCount());
        assertEquals(1, response.getTotalElements());
        assertEquals("DELIVERED", response.getData().get(0).getStatus().toString());
    }

    @Test
    void testInvalidWeightRangeThrowsException() {
        ShipmentSummaryRequest request = ShipmentSummaryRequest.builder()
                .minWeight(BigDecimal.valueOf(20))
                .maxWeight(BigDecimal.valueOf(10))
                .page(0)
                .size(10)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> shipmentService.getShipmentSummary(request));
    }

    @Test
    void testInvalidStatusThrowsException() {
        ShipmentSummaryRequest request = ShipmentSummaryRequest.builder()
                .status("UNKNOWN")
                .page(0)
                .size(10)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> shipmentService.getShipmentSummary(request));
    }



}