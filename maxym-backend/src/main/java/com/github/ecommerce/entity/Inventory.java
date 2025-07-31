import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_inventory_product", columnList = "product_id"),
    @Index(name = "idx_inventory_variant", columnList = "product_variant_id"),
    @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"product", "productVariant"})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "warehouse_location", length = 50)
    @Builder.Default
    private String warehouseLocation = "MAIN";

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(name = "reorder_point")
    @Builder.Default
    private Integer reorderPoint = 10;

    @Column(name = "reorder_quantity")
    @Builder.Default
    private Integer reorderQuantity = 50;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Column(name = "track_inventory", nullable = false)
    @Builder.Default
    private Boolean trackInventory = true;

    @Column(name = "allow_backorder", nullable = false)
    @Builder.Default
    private Boolean allowBackorder = false;

    @Column(name = "backorder_quantity")
    @Builder.Default
    private Integer backorderQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 20)
    @Builder.Default
    private StockStatus stockStatus = StockStatus.OUT_OF_STOCK;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "last_sold_at")
    private LocalDateTime lastSoldAt;

    @Column(name = "stock_alert_sent_at")
    private LocalDateTime stockAlertSentAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateCalculatedFields() {
        // Calculate available quantity
        this.availableQuantity = quantity - reservedQuantity;
        
        // Update stock status
        if (availableQuantity > reorderPoint) {
            this.stockStatus = StockStatus.IN_STOCK;
        } else if (availableQuantity > 0) {
            this.stockStatus = StockStatus.LOW_STOCK;
        } else if (allowBackorder) {
            this.stockStatus = StockStatus.BACKORDER;
        } else {
            this.stockStatus = StockStatus.OUT_OF_STOCK;
        }
    }

    // Helper methods
    public boolean isAvailable() {
        return availableQuantity > 0 || allowBackorder;
    }

    public boolean isInStock() {
        return availableQuantity > 0;
    }

    public boolean needsReorder() {
        return trackInventory && availableQuantity <= reorderPoint;
    }

    public void reserveStock(int quantity) {
        if (quantity > availableQuantity && !allowBackorder) {
            throw new IllegalArgumentException("Insufficient stock available");
        }
        this.reservedQuantity += quantity;
        updateCalculatedFields();
    }

    public void releaseStock(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
        updateCalculatedFields();
    }

    public void commitStock(int quantity) {
        this.quantity -= quantity;
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
        this.lastSoldAt = LocalDateTime.now();
        updateCalculatedFields();
    }

    public void addStock(int quantity) {
        this.quantity += quantity;
        this.lastRestockedAt = LocalDateTime.now();
        updateCalculatedFields();
    }

    public void adjustStock(int newQuantity) {
        this.quantity = newQuantity;
        updateCalculatedFields();
    }

    public enum StockStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK,
        BACKORDER,
        DISCONTINUED
    }
}