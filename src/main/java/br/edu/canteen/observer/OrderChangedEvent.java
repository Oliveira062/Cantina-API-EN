package br.edu.canteen.observer;

import br.edu.canteen.model.OrderStatus;

public record OrderChangedEvent(Long orderId, Long customerId, String customerName, OrderStatus status,
                                   String itemsSummary) {
}
