package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images", indexes = {
    @Index(name = "idx_product_image_product", columnList = "product_id"),
    @Index(name = "idx_product_image_order", columnList = "display_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"product"})
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String url;

    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "format", length = 10)
    private String format;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public String getThumbnailUrl(int width, int height) {
        if (cloudinaryPublicId != null) {
            return String.format("https://res.cloudinary.com/%s/image/upload/w_%d,h_%d,c_fill/%s",
                    "your-cloud-name", width, height, cloudinaryPublicId);
        }
        return url;
    }

    public String getOptimizedUrl() {
        if (cloudinaryPublicId != null) {
            return String.format("https://res.cloudinary.com/%s/image/upload/q_auto,f_auto/%s",
                    "your-cloud-name", cloudinaryPublicId);
        }
        return url;
    }
}