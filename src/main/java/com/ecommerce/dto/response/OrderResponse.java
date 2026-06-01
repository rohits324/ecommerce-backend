package com.ecommerce.dto.response;

import com.ecommerce.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private List<OrderItemResponse> orderItems;

    // Shipping address snapshot
    private String shippingFullName;
    private String shippingPhone;
    private String shippingStreet;
    private String shippingStreet2;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingCountry;

    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
