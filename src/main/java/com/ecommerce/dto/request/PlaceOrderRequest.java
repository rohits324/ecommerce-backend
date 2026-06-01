package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Shipping address ID is required")
    private Long addressId;

    private String notes;
}
