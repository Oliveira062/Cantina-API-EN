package br.edu.canteen.controller;

import br.edu.canteen.dto.StaffRequest;
import br.edu.canteen.dto.UserStatusRequest;
import br.edu.canteen.dto.UserResponse;
import br.edu.canteen.model.User;
import br.edu.canteen.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "Account management, administrators only")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creates a staff account (cashier, kitchen or administrator)")
    public UserResponse create(@Valid @RequestBody StaffRequest data) {
        return userService.createStaff(data);
    }

    @GetMapping
    @Operation(summary = "Lists all users")
    public List<UserResponse> list() {
        return userService.list();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activates or deactivates an account")
    public UserResponse changeStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest data,
                                         @AuthenticationPrincipal User admin) {
        return userService.changeStatus(id, data.active(), admin);
    }
}
