package br.edu.canteen.dto;

import br.edu.canteen.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(Long productCode, String product, int quantity, BigDecimal unitPrice,
                                 BigDecimal subtotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProduct().getId(), item.getProduct().getName(), item.getQuantity(),
                item.getUnitPrice(), item.getSubtotal());
    }
}
