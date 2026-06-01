package com.ecommerce.service;

import com.ecommerce.dto.request.PlaceOrderRequest;
import com.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Transactional
    public OrderResponse placeOrder(UserPrincipal principal, PlaceOrderRequest request) {
        Cart cart = cartRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty. Add items before placing an order.");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));

        if (!address.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("This address does not belong to you");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        // Validate stock and compute total
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Build Order
        Order order = Order.builder()
                .user(user)
                .totalAmount(total)
                .shippingFullName(address.getFullName())
                .shippingPhone(address.getPhone())
                .shippingStreet(address.getStreet())
                .shippingStreet2(address.getStreet2())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingZipCode(address.getZipCode())
                .shippingCountry(address.getCountry())
                .notes(request.getNotes())
                .paymentId("MOCK-PAY-" + System.currentTimeMillis())
                .paymentStatus("MOCK_PAID")
                .build();

        order = orderRepository.save(order);

        // Build OrderItems and deduct stock
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice())
                    .productName(product.getName())
                    .build();
            order.getOrderItems().add(orderItem);

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        order = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(cart);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(UserPrincipal principal, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(principal.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UserPrincipal principal, Long orderId) {
        Order order = findById(orderId);
        // User can see their own orders; admin can see all
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("Access denied to this order");
        }
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UserPrincipal principal, Long orderId) {
        Order order = findById(orderId);

        if (!order.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled at status: " + order.getStatus());
        }

        // Restore stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findById(orderId);
        order.setStatus(request.getStatus());
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream().map(item ->
                OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build()
        ).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .orderItems(items)
                .shippingFullName(order.getShippingFullName())
                .shippingPhone(order.getShippingPhone())
                .shippingStreet(order.getShippingStreet())
                .shippingStreet2(order.getShippingStreet2())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingZipCode(order.getShippingZipCode())
                .shippingCountry(order.getShippingCountry())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
