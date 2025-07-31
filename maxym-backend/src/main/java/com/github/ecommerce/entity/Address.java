package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_address_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user"})
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AddressType type;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(name = "address_line1", nullable = false, length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 2)
    private String country;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(precision = 10, scale = 6)
    private Double latitude;

    @Column(precision = 11, scale = 6)
    private Double longitude;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void ensureSingleDefault() {
        // This logic should be handled in the service layer to ensure only one default address per user
        if (isDefault == null) {
            isDefault = false;
        }
    }

    // Helper methods
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName).append("\n");
        if (companyName != null) {
            sb.append(companyName).append("\n");
        }
        sb.append(addressLine1).append("\n");
        if (addressLine2 != null) {
            sb.append(addressLine2).append("\n");
        }
        sb.append(city).append(", ");
        if (state != null) {
            sb.append(state).append(" ");
        }
        sb.append(postalCode).append("\n");
        sb.append(country);
        return sb.toString();
    }

    public String getShortAddress() {
        return city + ", " + (state != null ? state + ", " : "") + country;
    }

    public enum AddressType {
        SHIPPING,
        BILLING,
        BOTH
    }
}