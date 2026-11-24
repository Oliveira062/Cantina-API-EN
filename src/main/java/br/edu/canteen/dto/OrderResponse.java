package br.edu.canteen.dto;

import br.edu.canteen.model.Order;
import br.edu.canteen.model.OrderStatus;
import br.edu.canteen.payment.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long number, Long customerId, String customer, OrderStatus status,
                             String statusDescription, List<OrderItemResponse> items, BigDecimal subtotal,
                             BigDecimal total, PaymentType payment, BigDecimal change, Instant createdAt,
                             Instant updatedAt) {

    public static OrderResponse from(Order order, BigDecimal serviceFee) {
        List<OrderItemResponse> items = order.getItems().stream().map(OrderItemResponse::from).toList();
        return new OrderResponse(order.getId(), order.getCustomer().getId(), order.getCustomer().getName(),
                order.getStatus(), order.getStatus().getDescription(), items, order.getSubtotal(),
                order.calculateTotal(serviceFee), order.getPaymentType(), order.getChange(),
                order.getCreatedAt(), order.getUpdatedAt());
    }
}
