package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "product_variants", indexes = {
    @Index(name = "idx_variant_product", columnList = "product_id"),
    @Index(name = "idx_variant_sku", columnList = "sku", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"product", "inventory"})
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(length = 50)
    private String dimensions;

    @Column(length = 100)
    private String barcode;

    @ElementCollection
    @CollectionTable(name = "variant_attributes", joinColumns = @JoinColumn(name = "variant_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    @OneToOne(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Inventory inventory;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public BigDecimal getEffectivePrice() {
        if (price != null) {
            return price;
        }
        return product.getPrice();
    }

    public boolean isOnSale() {
        BigDecimal effectivePrice = getEffectivePrice();
        BigDecimal effectiveComparePrice = compareAtPrice != null ? compareAtPrice : product.getCompareAtPrice();
        
        return effectiveComparePrice != null && effectiveComparePrice.compareTo(effectivePrice) > 0;
    }

    public String getFullName() {
        return product.getName() + " - " + name;
    }

    public boolean isAvailable() {
        return active && inventory != null && inventory.isAvailable();
    }

    public String getAttributesDisplay() {
        if (attributes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        attributes.forEach((key, value) -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append(key).append(": ").append(value);
        });
        return sb.toString();
    }
}