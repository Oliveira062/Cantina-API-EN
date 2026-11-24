package br.edu.canteen.dto;

import java.math.BigDecimal;

public record PaymentResponse(boolean approved, String message, BigDecimal change, OrderResponse order) {
}
