package br.edu.canteen.service;

import br.edu.canteen.dto.NotificationResponse;
import br.edu.canteen.exception.ResourceNotFoundException;
import br.edu.canteen.model.Notification;
import br.edu.canteen.model.User;
import br.edu.canteen.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(User user) {
        return repository.findTop50ByUserIdOrTargetRoleOrderByIdDesc(user.getId(), user.getRole())
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, User user) {
        Notification notification = repository.findById(id)
                .filter(n -> n.isFor(user))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        notification.markAsRead();
        return NotificationResponse.from(notification);
    }
}
