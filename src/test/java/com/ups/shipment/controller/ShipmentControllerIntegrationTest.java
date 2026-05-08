package com.ups.shipment.controller;

import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.repository.ShipmentRepository;
import com.ups.shipment.entity.Shipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShipmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShipmentRepository shipmentRepository;

    private UUID testShipmentId;

    @BeforeEach
    void setUp() {
        shipmentRepository.deleteAll();

        Shipment shipment = Shipment.builder()
                .shipmentId(UUID.randomUUID())
                .orderId("ORD100")
                .sourceAddress("Tiruppur")
                .destinationAddress("Chennai")
                .weight(BigDecimal.valueOf(12.5))
                .status(ShipmentStatus.DELIVERED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        shipment = shipmentRepository.save(shipment);
        testShipmentId = shipment.getShipmentId();
    }

    @Test
    void testGetShipmentById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/shipments/{shipmentId}", testShipmentId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(testShipmentId.toString()))
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.weight").value(12.5));
    }

    @Test
    void testGetShipmentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/shipments/{shipmentId}", UUID.randomUUID().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetShipmentById_InvalidUUID() throws Exception {
        mockMvc.perform(get("/api/v1/shipments/{shipmentId}", "invalid-uuid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
