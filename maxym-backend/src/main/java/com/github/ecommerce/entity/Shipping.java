package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping", indexes = {
    @Index(name = "idx_shipping_order", columnList = "order_id"),
    @Index(name = "idx_shipping_tracking", columnList = "tracking_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"order"})
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShippingMethod method;

    @Column(nullable = false, length = 100)
    private String carrier;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "tracking_url")
    private String trackingUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ShippingStatus status = ShippingStatus.PENDING;

    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 50)
    private String dimensions;

    @Column(name = "insurance_amount", precision = 10, scale = 2)
    private BigDecimal insuranceAmount;

    @Column(name = "signature_required", nullable = false)
    @Builder.Default
    private Boolean signatureRequired = false;

    @Column(name = "label_url")
    private String labelUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @PreUpdate
    public void preUpdate() {
        updateStatusTimestamps();
    }

    private void updateStatusTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case SHIPPED:
                if (shippedAt == null) shippedAt = now;
                break;
            case DELIVERED:
                if (deliveredAt == null) {
                    deliveredAt = now;
                    actualDeliveryDate = now;
                }
                break;
        }
    }

    // Helper methods
    public boolean isDelivered() {
        return status == ShippingStatus.DELIVERED;
    }

    public boolean isInTransit() {
        return status == ShippingStatus.SHIPPED || status == ShippingStatus.IN_TRANSIT;
    }

    public boolean canBeCancelled() {
        return status == ShippingStatus.PENDING || status == ShippingStatus.PROCESSING;
    }

    public String getTrackingInfo() {
        return carrier + " - " + trackingNumber;
    }

    public enum ShippingMethod {
        STANDARD,
        EXPRESS,
        OVERNIGHT,
        PRIORITY,
        ECONOMY,
        FREE_SHIPPING,
        STORE_PICKUP,
        LOCAL_DELIVERY
    }

    public enum ShippingStatus {
        PENDING,
        PROCESSING,
        SHIPPED,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED,
        RETURNED,
        CANCELLED
    }
}