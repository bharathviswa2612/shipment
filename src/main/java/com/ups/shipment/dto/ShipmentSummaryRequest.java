package com.ups.shipment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentSummaryRequest {

    // Optional filters
    @Schema(description = "status description")
    private String status;

    @Positive
    private BigDecimal minWeight;

    @Positive
    private BigDecimal maxWeight;

    // Pagination (required)
    @Min(0)
    private int page;

    @Max(100)
    private int size;

    // Sorting (optional)
    private String sortBy;     // e.g., "createdAt" or "weight"
    private String sortOrder;  // "ASC" or "DESC"
}
