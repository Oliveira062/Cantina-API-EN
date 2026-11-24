package br.edu.canteen.controller;

import br.edu.canteen.dto.NotificationResponse;
import br.edu.canteen.model.User;
import br.edu.canteen.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notices about order status changes")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Latest 50 notifications of the current user")
    public List<NotificationResponse> list(@AuthenticationPrincipal User user) {
        return notificationService.list(user);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marks a notification as read")
    public NotificationResponse markAsRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return notificationService.markAsRead(id, user);
    }
}
