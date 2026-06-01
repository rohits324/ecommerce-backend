package com.ecommerce.config;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initialization.");
            return;
        }

        log.info("🌱 Seeding database with demo data...");

        // ===== Admin User =====
        User admin = User.builder()
                .name("Admin User")
                .email("admin@ecommerce.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        admin = userRepository.save(admin);
        Cart adminCart = Cart.builder().user(admin).build();
        cartRepository.save(adminCart);

        // ===== Customer User =====
        User customer = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .build();
        customer = userRepository.save(customer);
        Cart customerCart = Cart.builder().user(customer).build();
        cartRepository.save(customerCart);

        log.info("✅ Users created: admin@ecommerce.com (admin123) | john@example.com (password123)");

        // ===== Categories =====
        Category electronics = categoryRepository.save(Category.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Latest gadgets, laptops, phones and more")
                .build());

        Category clothing = categoryRepository.save(Category.builder()
                .name("Clothing")
                .slug("clothing")
                .description("Fashion for men, women and kids")
                .build());

        Category books = categoryRepository.save(Category.builder()
                .name("Books")
                .slug("books")
                .description("Best sellers, textbooks, and more")
                .build());

        Category homeGarden = categoryRepository.save(Category.builder()
                .name("Home & Garden")
                .slug("home-garden")
                .description("Everything for your home")
                .build());

        log.info("✅ 4 categories created");

        // ===== Products =====
        List<Product> products = List.of(
            Product.builder().name("Apple MacBook Pro 16\"").description("M3 Pro chip, 18GB RAM, 512GB SSD — the ultimate developer machine.")
                    .price(new BigDecimal("2499.99")).stockQuantity(15).category(electronics)
                    .imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400").build(),

            Product.builder().name("Sony WH-1000XM5 Headphones").description("Industry-leading noise cancellation with crystal-clear sound.")
                    .price(new BigDecimal("349.99")).stockQuantity(42).category(electronics)
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400").build(),

            Product.builder().name("Samsung Galaxy S24 Ultra").description("200MP camera, titanium build, S-Pen included.")
                    .price(new BigDecimal("1299.99")).stockQuantity(28).category(electronics)
                    .imageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400").build(),

            Product.builder().name("iPad Pro 12.9\"").description("M4 chip, Liquid Retina XDR display.")
                    .price(new BigDecimal("1099.99")).stockQuantity(20).category(electronics)
                    .imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400").build(),

            Product.builder().name("Men's Premium Slim Fit Suit").description("Italian wool blend, perfect for formal occasions.")
                    .price(new BigDecimal("299.99")).stockQuantity(50).category(clothing)
                    .imageUrl("https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=400").build(),

            Product.builder().name("Women's Cashmere Sweater").description("100% pure cashmere, incredibly soft.")
                    .price(new BigDecimal("149.99")).stockQuantity(75).category(clothing)
                    .imageUrl("https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=400").build(),

            Product.builder().name("Nike Air Max 270").description("Iconic Air Max cushioning for all-day comfort.")
                    .price(new BigDecimal("139.99")).stockQuantity(100).category(clothing)
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400").build(),

            Product.builder().name("Clean Code by Robert C. Martin").description("A handbook of agile software craftsmanship.")
                    .price(new BigDecimal("34.99")).stockQuantity(200).category(books)
                    .imageUrl("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400").build(),

            Product.builder().name("Spring Boot in Action").description("Covers Spring Boot 3 with practical examples.")
                    .price(new BigDecimal("49.99")).stockQuantity(150).category(books)
                    .imageUrl("https://images.unsplash.com/photo-1589998059171-988d887df646?w=400").build(),

            Product.builder().name("Design Patterns — GoF").description("Elements of reusable object-oriented software.")
                    .price(new BigDecimal("44.99")).stockQuantity(120).category(books)
                    .imageUrl("https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400").build(),

            Product.builder().name("Philips Hue Smart Bulb Starter Kit").description("Voice & app controlled, 16M colors.")
                    .price(new BigDecimal("79.99")).stockQuantity(60).category(homeGarden)
                    .imageUrl("https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400").build(),

            Product.builder().name("Dyson V15 Detect Cordless Vacuum").description("Laser dust detection, 60 min runtime.")
                    .price(new BigDecimal("699.99")).stockQuantity(18).category(homeGarden)
                    .imageUrl("https://images.unsplash.com/photo-1558618047-3c8a9acb10d8?w=400").build()
        );

        productRepository.saveAll(products);
        log.info("✅ 12 products created");
        log.info("🚀 Database seeding complete!");
        log.info("📖 Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("🗄️  H2 Console: http://localhost:8080/h2-console");
    }
}
