package br.edu.canteen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemRequest(
        @NotNull(message = "Enter the product code.")
        @Positive(message = "Invalid product code.")
        Long productCode,

        @NotNull(message = "Enter the quantity.")
        @Min(value = 1, message = "The quantity must be greater than zero.")
        @Max(value = 50, message = "The maximum quantity per item is 50.")
        Integer quantity
) {
}
