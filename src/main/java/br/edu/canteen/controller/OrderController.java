package br.edu.canteen.controller;

import br.edu.canteen.dto.CancellationResponse;
import br.edu.canteen.dto.HistoryResponse;
import br.edu.canteen.dto.ItemRequest;
import br.edu.canteen.dto.NewOrderRequest;
import br.edu.canteen.dto.PaymentRequest;
import br.edu.canteen.dto.PaymentResponse;
import br.edu.canteen.dto.OrderResponse;
import br.edu.canteen.model.OrderStatus;
import br.edu.canteen.model.User;
import br.edu.canteen.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Full order cycle, from cart to delivery")
public class OrderController {

    private static final String ORDER_BUILDERS = "hasAnyRole('CUSTOMER', 'CASHIER', 'ADMIN')";

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ORDER_BUILDERS)
    @Operation(summary = "Opens an order (customers for themselves; cashiers send the customerId)")
    public OrderResponse create(@Valid @RequestBody(required = false) NewOrderRequest data,
                                @AuthenticationPrincipal User user) {
        Long customerId = data == null ? null : data.customerId();
        return orderService.create(customerId, user);
    }

    @GetMapping
    @Operation(summary = "Lists orders (customers see only their own)")
    public List<OrderResponse> list(@RequestParam(required = false) OrderStatus status,
                                       @AuthenticationPrincipal User user) {
        return orderService.list(status, user);
    }

    @GetMapping("/{number}")
    @Operation(summary = "Gets an order")
    public OrderResponse get(@PathVariable Long number, @AuthenticationPrincipal User user) {
        return orderService.get(number, user);
    }

    @GetMapping("/{number}/history")
    @Operation(summary = "Shows every status change of the order")
    public List<HistoryResponse> history(@PathVariable Long number, @AuthenticationPrincipal User user) {
        return orderService.history(number, user);
    }

    @PostMapping("/{number}/items")
    @PreAuthorize(ORDER_BUILDERS)
    @Operation(summary = "Adds an item to the order")
    public OrderResponse addItem(@PathVariable Long number, @Valid @RequestBody ItemRequest data,
                                        @AuthenticationPrincipal User user) {
        return orderService.addItem(number, data.productCode(), data.quantity(), user);
    }

    @DeleteMapping("/{number}/items/{productCode}")
    @PreAuthorize(ORDER_BUILDERS)
    @Operation(summary = "Removes an item from the order")
    public OrderResponse removeItem(@PathVariable Long number, @PathVariable Long productCode,
                                      @AuthenticationPrincipal User user) {
        return orderService.removeItem(number, productCode, user);
    }

    @PostMapping("/{number}/checkout")
    @PreAuthorize(ORDER_BUILDERS)
    @Operation(summary = "Closes the cart and leaves the order awaiting payment")
    public OrderResponse checkout(@PathVariable Long number, @AuthenticationPrincipal User user) {
        return orderService.checkout(number, user);
    }

    @PostMapping("/{number}/payment")
    @PreAuthorize(ORDER_BUILDERS)
    @Operation(summary = "Registers the payment (PIX, CARD or CASH)")
    public PaymentResponse pay(@PathVariable Long number, @Valid @RequestBody PaymentRequest data,
                                   @AuthenticationPrincipal User user) {
        return orderService.pay(number, data.type(), data.amountReceived(), user);
    }

    @PostMapping("/{number}/preparation")
    @PreAuthorize("hasAnyRole('KITCHEN', 'ADMIN')")
    @Operation(summary = "The kitchen starts preparing")
    public OrderResponse startPreparation(@PathVariable Long number, @AuthenticationPrincipal User user) {
        return orderService.startPreparation(number, user);
    }

    @PostMapping("/{number}/ready")
    @PreAuthorize("hasAnyRole('KITCHEN', 'ADMIN')")
    @Operation(summary = "The kitchen marks it as ready")
    public OrderResponse markReady(@PathVariable Long number, @AuthenticationPrincipal User user) {
        return orderService.markReady(number, user);
    }

    @PostMapping("/{number}/delivery")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
    @Operation(summary = "The cashier confirms delivery")
    public OrderResponse confirmDelivery(@PathVariable Long number, @AuthenticationPrincipal User user) {
        return orderService.confirmDelivery(number, user);
    }

    @PostMapping("/{number}/cancel")
    @PreAuthorize(ORDER_BUILDERS)
    @Operation(summary = "Cancels the order (customers only before paying)")
    public CancellationResponse cancel(@PathVariable Long number, @AuthenticationPrincipal User user) {
        return orderService.cancel(number, user);
    }
}
