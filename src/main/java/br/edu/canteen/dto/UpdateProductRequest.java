package br.edu.canteen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @NotBlank(message = "Enter the product name.")
        @Size(max = 60, message = "The name must have at most 60 characters.")
        String name,

        @Size(max = 200, message = "The description must have at most 200 characters.")
        String description
) {
}
