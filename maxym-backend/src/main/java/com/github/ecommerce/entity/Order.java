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
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "items", "payment", "shipping"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "subtotal_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "EUR";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Shipping shipping;

    @Embedded
    private OrderAddress shippingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fullName", column = @Column(name = "billing_full_name")),
        @AttributeOverride(name = "addressLine1", column = @Column(name = "billing_address_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "billing_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code")),
        @AttributeOverride(name = "country", column = @Column(name = "billing_country")),
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "billing_phone_number"))
    })
    private OrderAddress billingAddress;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason")
    private String refundReason;

    @PrePersist
    public void prePersist() {
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        calculateTotalAmount();
    }

    @PreUpdate
    public void preUpdate() {
        calculateTotalAmount();
        updateStatusTimestamps();
    }

    // Helper methods
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void calculateTotalAmount() {
        this.totalAmount = subtotalAmount
            .add(taxAmount)
            .add(shippingAmount)
            .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    private void updateStatusTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case PAID:
                if (paidAt == null) paidAt = now;
                break;
            case SHIPPED:
                if (shippedAt == null) shippedAt = now;
                break;
            case DELIVERED:
                if (deliveredAt == null) deliveredAt = now;
                break;
            case CANCELLED:
                if (cancelledAt == null) cancelledAt = now;
                break;
            case REFUNDED:
                if (refundedAt == null) refundedAt = now;
                break;
        }
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.PROCESSING;
    }

    public boolean canBeRefunded() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.PAID;
    }

    public BigDecimal getItemsTotal() {
        return items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderAddress {
        @Column(length = 100)
        private String fullName;
        
        @Column(length = 200)
        private String addressLine1;
        
        @Column(length = 200)
        private String addressLine2;
        
        @Column(length = 100)
        private String city;
        
        @Column(length = 100)
        private String state;
        
        @Column(length = 20)
        private String postalCode;
        
        @Column(length = 2)
        private String country;
        
        @Column(length = 20)
        private String phoneNumber;
    }

    public enum OrderStatus {
        PENDING,
        PROCESSING,
        PAID,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED,
        FAILED
    }
}