package br.edu.canteen.dto;

import br.edu.canteen.model.OrderHistory;
import br.edu.canteen.model.OrderStatus;

import java.time.Instant;

public record HistoryResponse(OrderStatus from, OrderStatus to, String actor, Instant recordedAt) {

    public static HistoryResponse from(OrderHistory history) {
        return new HistoryResponse(history.getPreviousStatus(), history.getNewStatus(),
                history.getUser().getName(), history.getRecordedAt());
    }
}
