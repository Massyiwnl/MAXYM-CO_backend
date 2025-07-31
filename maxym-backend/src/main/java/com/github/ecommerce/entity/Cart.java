package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id"),
    @Index(name = "idx_cart_session", columnList = "session_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "items"})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;

    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (sessionId == null && user == null) {
            sessionId = UUID.randomUUID().toString();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(30);
        }
        updateTotals();
    }

    @PreUpdate
    public void preUpdate() {
        updateTotals();
    }

    // Helper methods
    public void updateTotals() {
        this.totalItems = items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        this.totalAmount = items.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    public void addItem(CartItem item) {
        // Check if product already exists in cart
        CartItem existingItem = items.stream()
            .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
            .findFirst()
            .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            item.setCart(this);
            items.add(item);
        }
        updateTotals();
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
        updateTotals();
    }

    public void updateItemQuantity(Long productId, Integer quantity) {
        items.stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .ifPresent(item -> {
                if (quantity <= 0) {
                    items.remove(item);
                } else {
                    item.setQuantity(quantity);
                }
            });
        updateTotals();
    }

    public void clear() {
        items.clear();
        couponCode = null;
        discountAmount = BigDecimal.ZERO;
        updateTotals();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public BigDecimal getSubtotal() {
        return items.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void applyCoupon(String couponCode, BigDecimal discountAmount) {
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
        updateTotals();
    }

    public void removeCoupon() {
        this.couponCode = null;
        this.discountAmount = BigDecimal.ZERO;
        updateTotals();
    }

    public void mergeWith(Cart otherCart) {
        if (otherCart != null && !otherCart.isEmpty()) {
            otherCart.getItems().forEach(item -> {
                CartItem newItem = CartItem.builder()
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build();
                addItem(newItem);
            });
        }
    }
}