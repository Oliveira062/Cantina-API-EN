package br.edu.canteen.controller;

import br.edu.canteen.dto.OrderResponse;
import br.edu.canteen.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@Tag(name = "Kitchen", description = "Preparation queue")
public class KitchenController {

    private final OrderService orderService;

    public KitchenController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('KITCHEN', 'ADMIN')")
    @Operation(summary = "Paid and in-preparation orders, oldest first")
    public List<OrderResponse> queue() {
        return orderService.kitchenQueue();
    }
}
