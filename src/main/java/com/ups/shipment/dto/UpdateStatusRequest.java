package com.ups.shipment.dto;

import com.ups.shipment.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequest {
    @NotNull
    private ShipmentStatus status;
}
