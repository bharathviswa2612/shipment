package com.ups.shipment.repository;

import com.ups.shipment.entity.Shipment;
import com.ups.shipment.entity.ShipmentStatus;
import com.ups.shipment.repository.projection.ShipmentAggregateProjection;
import com.ups.shipment.repository.projection.ShipmentSummaryProjection;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByShipmentId(UUID shipmentId);

    @Query("""
        SELECT 
            s.shipmentId AS shipmentId,
            s.status AS status,
            s.weight AS weight
        FROM Shipment s
        WHERE (:status IS NULL OR s.status = :status)
          AND (:minWeight IS NULL OR s.weight >= :minWeight)
          AND (:maxWeight IS NULL OR s.weight <= :maxWeight)
    """)
    Page<ShipmentSummaryProjection> findShipmentSummaries(
            @Param("status") ShipmentStatus status,
            @Param("minWeight") BigDecimal minWeight,
            @Param("maxWeight") BigDecimal maxWeight,
            Pageable pageable
    );

    @Query("""
        SELECT 
            COUNT(s) AS total,
            SUM(CASE WHEN s.status = 'DELIVERED' THEN 1 ELSE 0 END) AS deliveredCount,
            SUM(CASE WHEN s.status = 'IN_TRANSIT' THEN 1 ELSE 0 END) AS inTransitCount,
            SUM(CASE WHEN s.status = 'CANCELLED' THEN 1 ELSE 0 END) AS failedCount
        FROM Shipment s
        WHERE (:status IS NULL OR s.status = :status)
          AND (:minWeight IS NULL OR s.weight >= :minWeight)
          AND (:maxWeight IS NULL OR s.weight <= :maxWeight)
    """)
    ShipmentAggregateProjection getShipmentAggregates(
            @Param("status") ShipmentStatus status,
            @Param("minWeight") BigDecimal minWeight,
            @Param("maxWeight") BigDecimal maxWeight
    );

    boolean existsByOrderId(@NotBlank String orderId);
}
