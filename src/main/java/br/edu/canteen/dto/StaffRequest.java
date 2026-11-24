package br.edu.canteen.dto;

import br.edu.canteen.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StaffRequest(
        @NotBlank(message = "Enter the name.")
        @Size(min = 3, max = 100, message = "The name must have between 3 and 100 characters.")
        String name,

        @NotBlank(message = "Enter the email.")
        @Email(message = "Invalid email format.")
        @Size(max = 120, message = "The email must have at most 120 characters.")
        String email,

        @NotBlank(message = "Enter the password.")
        @Size(min = 8, max = 72, message = "The password must have between 8 and 72 characters.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "The password needs letters and numbers.")
        String password,

        @NotNull(message = "Enter the role.")
        Role role
) {
}
