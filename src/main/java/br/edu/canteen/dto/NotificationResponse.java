package br.edu.canteen.dto;

import br.edu.canteen.model.Notification;

import java.time.Instant;

public record NotificationResponse(Long id, Long order, String message, boolean read, Instant createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getOrderId(), notification.getMessage(),
                notification.isRead(), notification.getCreatedAt());
    }
}
