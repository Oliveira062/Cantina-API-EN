package br.edu.canteen.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank(message = "Enter the payment method: PIX, CARD or CASH.")
        String type,

        @DecimalMin(value = "0.00", message = "The amount received cannot be negative.")
        @Digits(integer = 6, fraction = 2, message = "Use at most two decimal places.")
        BigDecimal amountReceived
) {
}
