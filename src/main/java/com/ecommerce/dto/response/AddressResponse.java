package com.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String street;
    private String street2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
