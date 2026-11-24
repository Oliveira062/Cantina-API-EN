package br.edu.canteen.observer;

import br.edu.canteen.model.Notification;
import br.edu.canteen.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomerObserver implements OrderObserver {

    private static final Logger log = LoggerFactory.getLogger(CustomerObserver.class);

    private final NotificationRepository notificationRepository;

    public CustomerObserver(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refresh(OrderChangedEvent event) {
        try {
            String message = String.format("Hi, %s! Your order #%d is now: %s.",
                    event.customerName(), event.orderId(), event.status().getDescription());
            notificationRepository.save(Notification.forUser(event.customerId(), event.orderId(), message));
        } catch (RuntimeException e) {
            log.warn("Could not notify the customer of order #{}: {}", event.orderId(), e.getMessage());
        }
    }
}
