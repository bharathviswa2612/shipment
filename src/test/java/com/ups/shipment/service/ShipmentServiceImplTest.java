
package com.ups.shipment.service;

import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.dto.UpdateStatusResponse;
import com.ups.shipment.entity.Shipment;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.exceptionhandling.InvalidStatusTransitionException;
import com.ups.shipment.exceptionhandling.ShipmentNotFoundException;
import com.ups.shipment.repository.ShipmentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        Long shipmentId = 6875676L;

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
        Shipment shipment = Shipment.builder()
                .shipmentId(1L)
                .status(ShipmentStatus.CREATED)
                .build();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        UpdateStatusResponse response = shipmentService.updateStatus(1L, ShipmentStatus.PICKED);

        assertEquals(ShipmentStatus.PICKED, response.getStatus());
        assertEquals(1L, response.getShipmentId());
        assertNotNull(response.getUpdatedAt());
        assertEquals("Shipment status updated successfully", response.getMessage());
    }

    @Test
    void updateStatus_shipmentNotFound() {
        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ShipmentNotFoundException.class,
                () -> shipmentService.updateStatus(99L, ShipmentStatus.PICKED));
    }

    @Test
    void updateStatus_invalidTransition() {
        Shipment shipment = Shipment.builder()
                .shipmentId(1L)
                .status(ShipmentStatus.DELIVERED)
                .build();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        assertThrows(InvalidStatusTransitionException.class,
                () -> shipmentService.updateStatus(1L, ShipmentStatus.IN_TRANSIT));
    }

    @Test
    void updateStatus_sameStatusConflict() {
        Shipment shipment = Shipment.builder()
                .shipmentId(1L)
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        assertThrows(InvalidStatusTransitionException.class,
                () -> shipmentService.updateStatus(1L, ShipmentStatus.IN_TRANSIT));
    }

}