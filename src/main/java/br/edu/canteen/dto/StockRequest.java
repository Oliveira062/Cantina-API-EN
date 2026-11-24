package br.edu.canteen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotNull(message = "Enter the quantity.")
        @Min(value = 1, message = "The restock quantity must be greater than zero.")
        @Max(value = 10000, message = "The maximum restock at once is 10000.")
        Integer quantity
) {
}
