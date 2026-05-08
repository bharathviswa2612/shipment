package com.ups.shipment.repository.projection;

public interface ShipmentAggregateProjection {
    Long getTotal();
    Long getDeliveredCount();
    Long getInTransitCount();
    Long getFailedCount();
}