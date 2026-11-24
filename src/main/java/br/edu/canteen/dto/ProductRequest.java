package br.edu.canteen.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Enter the product name.")
        @Size(max = 60, message = "The name must have at most 60 characters.")
        String name,

        @Size(max = 200, message = "The description must have at most 200 characters.")
        String description,

        @NotNull(message = "Enter the price.")
        @DecimalMin(value = "0.01", message = "The price must be greater than zero.")
        @DecimalMax(value = "9999.99", message = "The maximum price is 9999.99.")
        @Digits(integer = 4, fraction = 2, message = "Use at most two decimal places.")
        BigDecimal price,

        @NotNull(message = "Enter the initial quantity.")
        @Min(value = 0, message = "The initial quantity cannot be negative.")
        @Max(value = 10000, message = "The maximum quantity is 10000.")
        Integer initialQuantity
) {
}
