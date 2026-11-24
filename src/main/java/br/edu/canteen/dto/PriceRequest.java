package br.edu.canteen.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PriceRequest(
        @NotNull(message = "Enter the new price.")
        @DecimalMin(value = "0.01", message = "The price must be greater than zero.")
        @DecimalMax(value = "9999.99", message = "The maximum price is 9999.99.")
        @Digits(integer = 4, fraction = 2, message = "Use at most two decimal places.")
        BigDecimal price
) {
}
