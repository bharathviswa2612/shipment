package com.ups.shipment.dto;

import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequest {

    @NotBlank(message = "OrderId must not be null or empty")
    private String orderId;

    @NotBlank(message = "Source address must not be null or empty")
    private String sourceAddress;

    @NotBlank(message = "Destination address must not be null or empty")
    private String destinationAddress;

    @NotNull(message = "Weight must not be null")
    @Positive(message = "Weight must be greater than 0")
    private BigDecimal weight;
}