package br.edu.canteen.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record SettingsRequest(
        @NotBlank(message = "Enter the canteen name.")
        @Size(max = 100, message = "The name must have at most 100 characters.")
        String name,

        @NotNull(message = "Enter the service fee (0 to 0.30).")
        @DecimalMin(value = "0.00", message = "The service fee cannot be negative.")
        @DecimalMax(value = "0.30", message = "The maximum service fee is 0.30 (30%).")
        BigDecimal serviceFee,

        @NotNull(message = "Enter the opening time.")
        LocalTime opensAt,

        @NotNull(message = "Enter the closing time.")
        LocalTime closesAt,

        boolean enforceHours
) {
}
