package br.edu.canteen.model;

import br.edu.canteen.exception.InsufficientStockException;
import br.edu.canteen.exception.InvalidOrderOperationException;
import br.edu.canteen.exception.EmptyOrderException;
import br.edu.canteen.exception.ProductNotFoundException;
import br.edu.canteen.payment.PaymentStrategy;
import br.edu.canteen.payment.PaymentResult;
import br.edu.canteen.payment.PaymentType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20)
    private PaymentType paymentType;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "change_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal change = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Order() {
    }

    public Order(User customer) {
        this.customer = Objects.requireNonNull(customer, "Every order needs a customer.");
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void addItem(Product product, int quantity) {
        requireStatus(OrderStatus.CREATED, "add items");
        if (quantity <= 0) {
            throw new InsufficientStockException("The quantity must be greater than zero.");
        }
        if (!product.isAvailable()) {
            throw new InsufficientStockException("The product " + product.getName() + " is unavailable right now.");
        }
        OrderItem existing = findItem(product.getId());
        int finalQuantity = quantity + (existing == null ? 0 : existing.getQuantity());
        if (!product.hasStock(finalQuantity)) {
            throw new InsufficientStockException(product.getName(), finalQuantity, product.getStockQuantity());
        }

        if (existing == null) {
            items.add(new OrderItem(this, product, quantity));
        } else {
            existing.increaseQuantity(quantity);
        }
        touch();
    }

    public void removeItem(Long productCode) {
        requireStatus(OrderStatus.CREATED, "remove items");
        OrderItem item = findItem(productCode);
        if (item == null) {
            throw new ProductNotFoundException(
                    String.format("The product with code %d is not in order #%d.", productCode, id));
        }
        items.remove(item);
        touch();
    }

    public BigDecimal getSubtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            sum = sum.add(item.getSubtotal());
        }
        return sum;
    }

    public BigDecimal calculateTotal(BigDecimal serviceFee) {
        if (totalAmount != null) {
            return totalAmount;
        }
        BigDecimal subtotal = getSubtotal();
        return subtotal.add(subtotal.multiply(serviceFee)).setScale(2, RoundingMode.HALF_UP);
    }

    public void checkout() {
        requireStatus(OrderStatus.CREATED, "check out");
        if (items.isEmpty()) {
            throw new EmptyOrderException(id);
        }
        validateStockForAllItems();
        changeStatus(OrderStatus.AWAITING_PAYMENT);
    }

    public PaymentResult registerPayment(PaymentStrategy strategy, BigDecimal amountReceived,
                                                 BigDecimal serviceFee) {
        if (items.isEmpty()) {
            throw new EmptyOrderException(id);
        }
        requireStatus(OrderStatus.AWAITING_PAYMENT, "pay");
        validateStockForAllItems();

        BigDecimal total = calculateTotal(serviceFee);
        PaymentResult result = strategy.process(total, amountReceived);
        if (!result.approved()) {
            return result;
        }

        for (OrderItem item : items) {
            item.getProduct().decreaseStock(item.getQuantity());
        }
        this.paymentType = strategy.getType();
        this.totalAmount = total;
        this.change = result.change();
        changeStatus(OrderStatus.PAID);
        return result;
    }

    public void changeStatus(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new InvalidOrderOperationException(String.format(
                    "Cannot change order #%d from \"%s\" to \"%s\".",
                    id, status.getDescription(), next.getDescription()));
        }
        status = next;
        touch();
    }

    public boolean cancel() {
        if (!status.canMoveTo(OrderStatus.CANCELED)) {
            throw new InvalidOrderOperationException(String.format(
                    "Order #%d is \"%s\" and cannot be canceled.", id, status.getDescription()));
        }
        boolean returnStock = status == OrderStatus.PAID;
        if (returnStock) {
            for (OrderItem item : items) {
                item.getProduct().returnStock(item.getQuantity());
            }
        }
        changeStatus(OrderStatus.CANCELED);
        return returnStock;
    }

    public boolean belongsTo(User user) {
        return customer.getId() != null && customer.getId().equals(user.getId());
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new InvalidOrderOperationException(String.format(
                    "You can only %s when the order is \"%s\" (current status: \"%s\").",
                    action, expected.getDescription(), status.getDescription()));
        }
    }

    private void validateStockForAllItems() {
        for (OrderItem item : items) {
            Product p = item.getProduct();
            if (!p.hasStock(item.getQuantity())) {
                throw new InsufficientStockException(p.getName(), item.getQuantity(), p.getStockQuantity());
            }
        }
    }

    private OrderItem findItem(Long productCode) {
        for (OrderItem item : items) {
            if (item.getProduct().getId().equals(productCode)) {
                return item;
            }
        }
        return null;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public OrderStatus getStatus() { return status; }
    public PaymentType getPaymentType() { return paymentType; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getChange() { return change; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
