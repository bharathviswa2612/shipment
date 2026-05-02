package com.ups.shipment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ups.shipment.dto.ShipmentRequest;
import com.ups.shipment.dto.ShipmentResponse;
import com.ups.shipment.dto.UpdateStatusRequest;
import com.ups.shipment.dto.UpdateStatusResponse;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.service.ShipmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShipmentService shipmentService;

    @Test
    @DisplayName("Should create shipment successfully and return response")
    void createShipment_success() throws Exception {
        // Arrange
        ShipmentResponse mockResponse = ShipmentResponse.builder()
                .shipmentId(1L)
                .orderId("ORD123")
                .sourceAddress("New York")
                .destinationAddress("Los Angeles")
                .weight(BigDecimal.valueOf(10.5))
                .status(ShipmentStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(shipmentService.createShipment(any(ShipmentRequest.class)))
                .thenReturn(mockResponse);

        ShipmentRequest request = ShipmentRequest.builder()
                .orderId("ORD123")
                .sourceAddress("New York")
                .destinationAddress("Los Angeles")
                .weight(BigDecimal.valueOf(10.5))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shipmentId").value(1L))
                .andExpect(jsonPath("$.orderId").value("ORD123"))
                .andExpect(jsonPath("$.sourceAddress").value("New York"))
                .andExpect(jsonPath("$.destinationAddress").value("Los Angeles"))
                .andExpect(jsonPath("$.weight").value(10.5))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when validation fails")
    void createShipment_validationError() throws Exception {
        // Arrange: invalid request (missing required fields)
        ShipmentRequest invalidRequest = ShipmentRequest.builder()
                .orderId("") // violates @NotBlank
                .sourceAddress("") // violates @NotBlank
                .destinationAddress("") // violates @NotBlank
                .weight(BigDecimal.valueOf(-5)) // violates @Positive
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateShipmentStatus_success() throws Exception {
        UpdateStatusResponse mockResponse = UpdateStatusResponse.builder()
                .shipmentId(1L)
                .status(ShipmentStatus.IN_TRANSIT)
                .updatedAt(LocalDateTime.now())
                .message("Shipment status updated successfully")
                .build();

        Mockito.when(shipmentService.updateStatus(eq(1L), any(ShipmentStatus.class)))
                .thenReturn(mockResponse);

        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        mockMvc.perform(patch("/api/v1/shipments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(1L))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.message").value("Shipment status updated successfully"));
    }

    @Test
    void updateShipmentStatus_invalidRequest() throws Exception {
        // Missing status field
        UpdateStatusRequest invalidRequest = new UpdateStatusRequest();

        mockMvc.perform(patch("/api/v1/shipments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
