package com.github.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "request_url")
    private String requestUrl;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public static AuditLog createLog(User user, AuditAction action, String entityType, Long entityId, String description) {
        return AuditLog.builder()
            .userId(user != null ? user.getId() : null)
            .username(user != null ? user.getEmail() : "SYSTEM")
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .description(description)
            .status(AuditStatus.SUCCESS)
            .build();
    }

    public void markAsFailed(String errorMessage) {
        this.status = AuditStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        PASSWORD_CHANGE,
        PASSWORD_RESET,
        EMAIL_VERIFICATION,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        PERMISSION_GRANTED,
        PERMISSION_REVOKED,
        PAYMENT_PROCESSED,
        PAYMENT_FAILED,
        ORDER_PLACED,
        ORDER_CANCELLED,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        REFUND_PROCESSED,
        EXPORT_DATA,
        IMPORT_DATA,
        SYSTEM_CONFIG_CHANGE
    }

    public enum AuditStatus {
        SUCCESS,
        FAILED,
        PENDING
    }
}