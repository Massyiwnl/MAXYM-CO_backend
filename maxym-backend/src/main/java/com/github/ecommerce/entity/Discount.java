package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "discounts", indexes = {
    @Index(name = "idx_discount_code", columnList = "code", unique = true),
    @Index(name = "idx_discount_active", columnList = "active"),
    @Index(name = "idx_discount_dates", columnList = "start_date,end_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"products", "categories", "usedByCoupons"})
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "minimum_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minimumPurchaseAmount;

    @Column(name = "maximum_discount_amount", precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", nullable = false, length = 20)
    @Builder.Default
    private AppliesTo appliesTo = AppliesTo.ALL;

    @ManyToMany(mappedBy = "discounts")
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "discount_categories",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "discount")
    @Builder.Default
    private Set<Coupon> usedByCoupons = new HashSet<>();

    @Column(name = "requires_coupon", nullable = false)
    @Builder.Default
    private Boolean requiresCoupon = true;

    @Column(name = "combine_with_other_discounts", nullable = false)
    @Builder.Default
    private Boolean combineWithOtherDiscounts = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && 
               now.isAfter(startDate) && 
               (endDate == null || now.isBefore(endDate)) &&
               (usageLimit == null || usageCount < usageLimit);
    }

    public boolean canBeUsedBy(User user, int userUsageCount) {
        return isValid() && 
               (usageLimitPerUser == null || userUsageCount < usageLimitPerUser);
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!isValid() || amount.compareTo(minimumPurchaseAmount != null ? minimumPurchaseAmount : BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (type == DiscountType.PERCENTAGE) {
            discount = amount.multiply(value).divide(new BigDecimal("100"));
        } else {
            discount = value;
        }

        if (maximumDiscountAmount != null && discount.compareTo(maximumDiscountAmount) > 0) {
            discount = maximumDiscountAmount;
        }

        return discount.min(amount);
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public boolean appliesToProduct(Product product) {
        switch (appliesTo) {
            case ALL:
                return true;
            case SPECIFIC_PRODUCTS:
                return products.contains(product);
            case SPECIFIC_CATEGORIES:
                return categories.contains(product.getCategory());
            default:
                return false;
        }
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }

    public enum AppliesTo {
        ALL,
        SPECIFIC_PRODUCTS,
        SPECIFIC_CATEGORIES
    }
}