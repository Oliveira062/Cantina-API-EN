package br.edu.canteen.service;

import br.edu.canteen.dto.CancellationResponse;
import br.edu.canteen.dto.HistoryResponse;
import br.edu.canteen.dto.PaymentResponse;
import br.edu.canteen.dto.OrderResponse;
import br.edu.canteen.exception.InvalidDataException;
import br.edu.canteen.exception.InvalidOrderOperationException;
import br.edu.canteen.exception.InvalidPaymentException;
import br.edu.canteen.exception.OrderNotFoundException;
import br.edu.canteen.model.OrderHistory;
import br.edu.canteen.model.Order;
import br.edu.canteen.model.Product;
import br.edu.canteen.model.OrderStatus;
import br.edu.canteen.model.User;
import br.edu.canteen.observer.OrderChangedEvent;
import br.edu.canteen.payment.PaymentStrategy;
import br.edu.canteen.payment.PaymentFactory;
import br.edu.canteen.payment.PaymentResult;
import br.edu.canteen.payment.PaymentType;
import br.edu.canteen.repository.OrderHistoryRepository;
import br.edu.canteen.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final List<OrderStatus> KITCHEN_QUEUE =
            List.of(OrderStatus.PAID, OrderStatus.PREPARING);

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository historyRepository;
    private final ProductService productService;
    private final UserService userService;
    private final SettingsService settingsService;
    private final PaymentFactory paymentFactory;
    private final ApplicationEventPublisher publisher;

    public OrderService(OrderRepository orderRepository, OrderHistoryRepository historyRepository,
                         ProductService productService, UserService userService,
                         SettingsService settingsService, PaymentFactory paymentFactory,
                         ApplicationEventPublisher publisher) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.productService = productService;
        this.userService = userService;
        this.settingsService = settingsService;
        this.paymentFactory = paymentFactory;
        this.publisher = publisher;
    }

    @Transactional
    public OrderResponse create(Long customerId, User requester) {
        settingsService.checkOpeningHours();
        User customer = resolveCustomer(customerId, requester);
        Order order = orderRepository.save(new Order(customer));
        historyRepository.save(new OrderHistory(order.getId(), null, OrderStatus.CREATED, requester));
        return respond(order);
    }

    @Transactional
    public OrderResponse addItem(Long number, Long productCode, int quantity, User user) {
        Order order = findWithAccess(number, user);
        Product product = productService.find(productCode);
        order.addItem(product, quantity);
        return respond(order);
    }

    @Transactional
    public OrderResponse removeItem(Long number, Long productCode, User user) {
        Order order = findWithAccess(number, user);
        order.removeItem(productCode);
        return respond(order);
    }

    @Transactional
    public OrderResponse checkout(Long number, User user) {
        Order order = findWithAccess(number, user);
        OrderStatus previous = order.getStatus();
        order.checkout();
        recordChange(order, previous, user);
        return respond(order);
    }

    @Transactional
    public PaymentResponse pay(Long number, String type, BigDecimal amountReceived, User user) {
        Order order = findWithAccess(number, user);
        PaymentType paymentType = PaymentType.from(type);
        if (paymentType == PaymentType.CASH && user.isCustomer()) {
            throw new InvalidPaymentException("Cash payments can only be registered at the counter, by the cashier.");
        }
        if (order.getStatus() == OrderStatus.CREATED) {
            checkoutWithoutResponse(order, user);
        }

        OrderStatus previous = order.getStatus();
        PaymentStrategy strategy = paymentFactory.create(paymentType);
        PaymentResult result = order.registerPayment(strategy, amountReceived,
                settingsService.serviceFee());
        if (result.approved()) {
            recordChange(order, previous, user);
        }
        return new PaymentResponse(result.approved(), result.message(), result.change(), respond(order));
    }

    @Transactional
    public OrderResponse startPreparation(Long number, User user) {
        Order order = findWithAccess(number, user);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidOrderOperationException(String.format(
                    "Only paid orders can go into preparation. Order #%d is \"%s\".",
                    number, order.getStatus().getDescription()));
        }
        return changeStatus(order, OrderStatus.PREPARING, user);
    }

    @Transactional
    public OrderResponse markReady(Long number, User user) {
        return changeStatus(findWithAccess(number, user), OrderStatus.READY, user);
    }

    @Transactional
    public OrderResponse confirmDelivery(Long number, User user) {
        Order order = findWithAccess(number, user);
        if (order.getStatus() != OrderStatus.READY) {
            throw new InvalidOrderOperationException(String.format(
                    "Only ready orders can be delivered. Order #%d is \"%s\".",
                    number, order.getStatus().getDescription()));
        }
        return changeStatus(order, OrderStatus.DELIVERED, user);
    }

    @Transactional
    public CancellationResponse cancel(Long number, User user) {
        Order order = findWithAccess(number, user);
        OrderStatus previous = order.getStatus();
        boolean customerCanCancel = previous == OrderStatus.CREATED
                || previous == OrderStatus.AWAITING_PAYMENT;
        if (user.isCustomer() && !customerCanCancel) {
            throw new InvalidOrderOperationException(
                    "After payment, cancellation must be done by the cashier at the counter.");
        }
        boolean returned = order.cancel();
        recordChange(order, previous, user);
        return new CancellationResponse(returned, respond(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long number, User user) {
        return respond(findWithAccess(number, user));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(OrderStatus status, User user) {
        List<Order> orders;
        if (user.isCustomer()) {
            orders = status == null
                    ? orderRepository.findByCustomerIdOrderByIdDesc(user.getId())
                    : orderRepository.findByCustomerIdAndStatusOrderByIdDesc(user.getId(), status);
        } else {
            orders = status == null
                    ? orderRepository.findAllByOrderByIdDesc()
                    : orderRepository.findByStatusOrderByIdDesc(status);
        }
        BigDecimal fee = settingsService.serviceFee();
        return orders.stream().map(p -> OrderResponse.from(p, fee)).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> kitchenQueue() {
        BigDecimal fee = settingsService.serviceFee();
        return orderRepository.findByStatusInOrderByIdAsc(KITCHEN_QUEUE).stream()
                .map(p -> OrderResponse.from(p, fee)).toList();
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> history(Long number, User user) {
        findWithAccess(number, user);
        return historyRepository.findByOrderIdOrderByIdAsc(number).stream().map(HistoryResponse::from).toList();
    }

    private User resolveCustomer(Long customerId, User requester) {
        if (requester.isCustomer()) {
            if (customerId != null && !customerId.equals(requester.getId())) {
                throw new InvalidDataException("Customers can only open orders for themselves.");
            }
            return requester;
        }
        if (customerId == null) {
            throw new InvalidDataException("Send the customerId to open an order at the counter.");
        }
        User customer = userService.find(customerId);
        if (!customer.isCustomer() || !customer.isActive()) {
            throw new InvalidDataException("The code does not belong to an active customer.");
        }
        return customer;
    }

    private Order findWithAccess(Long number, User user) {
        Order order = orderRepository.findById(number).orElseThrow(() -> new OrderNotFoundException(number));
        if (user.isCustomer() && !order.belongsTo(user)) {
            throw new OrderNotFoundException(number);
        }
        return order;
    }

    private void checkoutWithoutResponse(Order order, User user) {
        OrderStatus previous = order.getStatus();
        order.checkout();
        recordChange(order, previous, user);
    }

    private OrderResponse changeStatus(Order order, OrderStatus next, User user) {
        OrderStatus previous = order.getStatus();
        order.changeStatus(next);
        recordChange(order, previous, user);
        return respond(order);
    }

    private void recordChange(Order order, OrderStatus previous, User user) {
        historyRepository.save(new OrderHistory(order.getId(), previous, order.getStatus(), user));
        String summaryText = order.getItems().stream()
                .map(item -> item.getQuantity() + "x " + item.getProduct().getName())
                .collect(Collectors.joining(", "));
        publisher.publishEvent(new OrderChangedEvent(order.getId(), order.getCustomer().getId(),
                order.getCustomer().getName(), order.getStatus(), summaryText));
    }

    private OrderResponse respond(Order order) {
        return OrderResponse.from(order, settingsService.serviceFee());
    }
}
