package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}),
    indexes = {
        @Index(name = "idx_wishlist_user", columnList = "user_id"),
        @Index(name = "idx_wishlist_product", columnList = "product_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "product"})
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "notify_on_sale", nullable = false)
    @Builder.Default
    private Boolean notifyOnSale = false;

    @Column(name = "notify_on_restock", nullable = false)
    @Builder.Default
    private Boolean notifyOnRestock = false;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean shouldNotifyOnSale() {
        return notifyOnSale && product.isOnSale() && notifiedAt == null;
    }

    public boolean shouldNotifyOnRestock() {
        return notifyOnRestock && product.isAvailable() && notifiedAt == null;
    }

    public void markAsNotified() {
        this.notifiedAt = LocalDateTime.now();
    }
}