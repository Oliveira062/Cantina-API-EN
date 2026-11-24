package br.edu.canteen.controller;

import br.edu.canteen.dto.UpdateProductRequest;
import br.edu.canteen.dto.StockRequest;
import br.edu.canteen.dto.PriceRequest;
import br.edu.canteen.dto.ProductRequest;
import br.edu.canteen.dto.ProductResponse;
import br.edu.canteen.model.Role;
import br.edu.canteen.model.User;
import br.edu.canteen.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Menu", description = "Public menu and maintenance by the administrator")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lists the menu (administrators also see deactivated products)")
    public List<ProductResponse> list(@AuthenticationPrincipal User user) {
        return productService.listMenu(isAdmin(user));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Gets a product by code")
    public ProductResponse get(@PathVariable Long code, @AuthenticationPrincipal User user) {
        return productService.get(code, isAdmin(user));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Creates a product")
    public ProductResponse create(@Valid @RequestBody ProductRequest data) {
        return productService.create(data);
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Updates name and description")
    public ProductResponse refresh(@PathVariable Long code, @Valid @RequestBody UpdateProductRequest data) {
        return productService.updateDetails(code, data);
    }

    @PatchMapping("/{code}/price")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Updates the price")
    public ProductResponse updatePrice(@PathVariable Long code, @Valid @RequestBody PriceRequest data) {
        return productService.updatePrice(code, data.price());
    }

    @PostMapping("/{code}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restocks")
    public ProductResponse restock(@PathVariable Long code, @Valid @RequestBody StockRequest data) {
        return productService.restock(code, data.quantity());
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivates the product (it leaves the menu but stays in past orders)")
    public ProductResponse deactivate(@PathVariable Long code) {
        return productService.setActive(code, false);
    }

    @PostMapping("/{code}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivates a deactivated product")
    public ProductResponse reactivate(@PathVariable Long code) {
        return productService.setActive(code, true);
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }
}
