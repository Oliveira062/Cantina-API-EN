package br.edu.canteen.controller;

import br.edu.canteen.dto.LoginRequest;
import br.edu.canteen.dto.RegisterRequest;
import br.edu.canteen.dto.TokenResponse;
import br.edu.canteen.dto.UserResponse;
import br.edu.canteen.model.User;
import br.edu.canteen.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Customer sign-up, login and current user details")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a customer account")
    public UserResponse register(@Valid @RequestBody RegisterRequest data) {
        return authService.registerCustomer(data);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticates and returns the JWT token")
    public TokenResponse login(@Valid @RequestBody LoginRequest data) {
        return authService.login(data);
    }

    @GetMapping("/me")
    @Operation(summary = "Shows the details of the token owner")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return UserResponse.from(user);
    }
}
