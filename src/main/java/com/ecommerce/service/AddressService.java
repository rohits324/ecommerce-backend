package com.ecommerce.service;

import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.response.AddressResponse;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(UserPrincipal principal) {
        return addressRepository.findByUserId(principal.getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse addAddress(UserPrincipal principal, AddressRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (request.isDefault()) {
            // Unset existing default
            addressRepository.findByUserIdAndIsDefaultTrue(principal.getId())
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                        addressRepository.save(existing);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .street2(request.getStreet2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(UserPrincipal principal, Long addressId, AddressRequest request) {
        Address address = findByIdAndOwner(addressId, principal.getId());

        if (request.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(principal.getId())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(addressId)) {
                            existing.setDefault(false);
                            addressRepository.save(existing);
                        }
                    });
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setStreet2(request.getStreet2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UserPrincipal principal, Long addressId) {
        Address address = findByIdAndOwner(addressId, principal.getId());
        addressRepository.delete(address);
    }

    private Address findByIdAndOwner(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This address does not belong to you");
        }
        return address;
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .street2(address.getStreet2())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
