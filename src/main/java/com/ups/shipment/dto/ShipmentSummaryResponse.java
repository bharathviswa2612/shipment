package com.ups.shipment.dto;

import com.ups.shipment.entity.ShipmentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentSummaryResponse {

    // Pagination metadata
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    // Aggregated counts
    private long deliveredCount;
    private long inTransitCount;
    private long failedCount;

    // Shipment summary list
    private List<ShipmentSummaryData> data;

    // Inner DTO for each shipment row
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShipmentSummaryData {
        private UUID shipmentId;
        private ShipmentStatus status;
        private BigDecimal weight;
    }
}
