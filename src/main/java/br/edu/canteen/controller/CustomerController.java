package br.edu.canteen.controller;

import br.edu.canteen.dto.UserResponse;
import br.edu.canteen.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
@Tag(name = "Customers", description = "Customer search to open orders at the counter")
public class CustomerController {

    private final UserService userService;

    public CustomerController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Searches active customers by name, email or student ID (up to 20 results)")
    public List<UserResponse> find(@RequestParam(required = false) String search) {
        return userService.searchCustomers(search);
    }
}
