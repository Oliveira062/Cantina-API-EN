package br.edu.canteen.dto;

import jakarta.validation.constraints.Positive;

public record NewOrderRequest(
        @Positive(message = "Invalid customer code.")
        Long customerId
) {
}
