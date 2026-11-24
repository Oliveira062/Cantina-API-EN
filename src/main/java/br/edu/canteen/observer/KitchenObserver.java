package br.edu.canteen.observer;

import br.edu.canteen.model.Notification;
import br.edu.canteen.model.Role;
import br.edu.canteen.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class KitchenObserver implements OrderObserver {

    private static final Logger log = LoggerFactory.getLogger(KitchenObserver.class);

    private final NotificationRepository notificationRepository;

    public KitchenObserver(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refresh(OrderChangedEvent event) {
        String message = switch (event.status()) {
            case PAID -> "New order #" + event.orderId() + " to prepare: " + event.itemsSummary();
            case CANCELED -> "Order #" + event.orderId() + " was canceled.";
            default -> null;
        };
        if (message == null) {
            return;
        }
        try {
            notificationRepository.save(Notification.forRole(Role.KITCHEN, event.orderId(), message));
        } catch (RuntimeException e) {
            log.warn("Could not notify the kitchen about order #{}: {}", event.orderId(), e.getMessage());
        }
    }
}
