package br.edu.canteen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Enter the email.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "Enter the password.")
        String password
) {
}
