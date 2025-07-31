package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
    @Index(name = "idx_cart_item_product", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"cart", "product"})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (price == null && product != null) {
            price = product.getPrice();
        }
    }

    // Helper methods
    public BigDecimal getTotalPrice() {
        BigDecimal itemTotal = price.multiply(new BigDecimal(quantity));
        return itemTotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    public BigDecimal getUnitPrice() {
        return price;
    }

    public boolean isAvailable() {
        if (product == null || !product.isActive()) {
            return false;
        }
        
        if (productVariant != null) {
            return productVariant.getInventory() != null && 
                   productVariant.getInventory().getAvailableQuantity() >= quantity;
        }
        
        return product.getInventory() != null && 
               product.getInventory().getAvailableQuantity() >= quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    public String getProductName() {
        String name = product.getName();
        if (productVariant != null && productVariant.getName() != null) {
            name += " - " + productVariant.getName();
        }
        return name;
    }

    public String getProductImageUrl() {
        if (productVariant != null && productVariant.getImageUrl() != null) {
            return productVariant.getImageUrl();
        }
        if (!product.getImages().isEmpty()) {
            return product.getImages().get(0).getUrl();
        }
        return null;
    }
}