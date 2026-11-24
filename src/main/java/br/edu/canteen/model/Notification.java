package br.edu.canteen.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", length = 20)
    private Role targetRole;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
    }

    private Notification(Long userId, Role targetRole, Long orderId, String message) {
        this.userId = userId;
        this.targetRole = targetRole;
        this.orderId = orderId;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public static Notification forUser(Long userId, Long orderId, String message) {
        return new Notification(userId, null, orderId, message);
    }

    public static Notification forRole(Role role, Long orderId, String message) {
        return new Notification(null, role, orderId, message);
    }

    public boolean isFor(User user) {
        return user.getId().equals(userId) || user.getRole() == targetRole;
    }

    public void markAsRead() {
        this.read = true;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Role getTargetRole() { return targetRole; }
    public Long getOrderId() { return orderId; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
