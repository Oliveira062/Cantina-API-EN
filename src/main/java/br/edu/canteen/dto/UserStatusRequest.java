package br.edu.canteen.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull(message = "Tell whether the user stays active.")
        Boolean active
) {
}
