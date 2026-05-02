package com.ups.shipment.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String sourceAddress;

    @NotBlank
    private String destinationAddress;

    @NotNull
    @Positive
    private BigDecimal weight;
}