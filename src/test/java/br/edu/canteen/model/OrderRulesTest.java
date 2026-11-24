package br.edu.canteen.model;

import br.edu.canteen.exception.InvalidDataException;
import br.edu.canteen.exception.InsufficientStockException;
import br.edu.canteen.exception.InvalidOrderOperationException;
import br.edu.canteen.exception.EmptyOrderException;
import br.edu.canteen.exception.InsufficientAmountException;
import br.edu.canteen.payment.CardPayment;
import br.edu.canteen.payment.CashPayment;
import br.edu.canteen.payment.PixPayment;
import br.edu.canteen.payment.PaymentResult;
import br.edu.canteen.payment.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRulesTest {

    private static final BigDecimal NO_FEE = BigDecimal.ZERO;

    private Product croquette;
    private User ann;

    @BeforeEach
    void setUpScenario() {
        croquette = new Product("Croquette", "Chicken", new BigDecimal("6.00"), 20);
        ReflectionTestUtils.setField(croquette, "id", 1L);
        ann = new User("Ann", "ann@test.com", "hash", Role.CUSTOMER, "2024001");
        ReflectionTestUtils.setField(ann, "id", 10L);
    }

    private Order checkedOutOrderWithTwoCroquettes() {
        Order order = new Order(ann);
        order.addItem(croquette, 2);
        order.checkout();
        return order;
    }

    @Test
    @DisplayName("CT01 - a valid product is available")
    void ct01ValidProduct() {
        Product juice = new Product("Juice", "Orange", new BigDecimal("7.00"), 10);
        assertTrue(juice.isAvailable());
        assertEquals(10, juice.getStockQuantity());
    }

    @Test
    @DisplayName("CT02 - zero price is rejected")
    void ct02InvalidPrice() {
        assertThrows(InvalidDataException.class, () -> new Product("Candy", "Mint", BigDecimal.ZERO, 5));
    }

    @Test
    @DisplayName("CT03 - the same product added twice becomes a single item")
    void ct03AddItem() {
        Order order = new Order(ann);
        order.addItem(croquette, 2);
        order.addItem(croquette, 1);
        assertEquals(1, order.getItems().size());
        assertEquals(0, new BigDecimal("18.00").compareTo(order.calculateTotal(NO_FEE)));
    }

    @Test
    @DisplayName("CT04 - quantity above the stock is rejected")
    void ct04InsufficientStock() {
        Order order = new Order(ann);
        assertThrows(InsufficientStockException.class, () -> order.addItem(croquette, 25));
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    @DisplayName("CT05 - an empty order cannot be checked out")
    void ct05EmptyOrder() {
        Order order = new Order(ann);
        assertThrows(EmptyOrderException.class, order::checkout);
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    @DisplayName("CT06 - PIX payment decreases the stock")
    void ct06PayWithPix() {
        Order order = checkedOutOrderWithTwoCroquettes();
        order.registerPayment(new PixPayment(), null, NO_FEE);
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(18, croquette.getStockQuantity());
    }

    @Test
    @DisplayName("CT07 - card payment records the type used")
    void ct07PayWithCard() {
        Order order = checkedOutOrderWithTwoCroquettes();
        order.registerPayment(new CardPayment(), null, NO_FEE);
        assertEquals(PaymentType.CARD, order.getPaymentType());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("CT08 - insufficient cash is rejected and change is calculated")
    void ct08InsufficientCash() {
        Order order = checkedOutOrderWithTwoCroquettes();
        assertThrows(InsufficientAmountException.class,
                () -> order.registerPayment(new CashPayment(), new BigDecimal("5.00"), NO_FEE));
        assertEquals(OrderStatus.AWAITING_PAYMENT, order.getStatus());
        assertEquals(20, croquette.getStockQuantity());

        PaymentResult result =
                order.registerPayment(new CashPayment(), new BigDecimal("20.00"), NO_FEE);
        assertEquals(0, new BigDecimal("8.00").compareTo(result.change()));
    }

    @Test
    @DisplayName("CT10 - status changes follow the lifecycle order")
    void ct10Lifecycle() {
        Order order = checkedOutOrderWithTwoCroquettes();
        order.registerPayment(new PixPayment(), null, NO_FEE);
        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);
        assertThrows(InvalidOrderOperationException.class, () -> order.changeStatus(OrderStatus.PAID));
        assertEquals(OrderStatus.READY, order.getStatus());
    }

    @Test
    @DisplayName("CT11 - a paid order cannot be delivered yet")
    void ct11DeliveryTooEarly() {
        Order order = checkedOutOrderWithTwoCroquettes();
        order.registerPayment(new PixPayment(), null, NO_FEE);
        assertThrows(InvalidOrderOperationException.class, () -> order.changeStatus(OrderStatus.DELIVERED));
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("CT12 - canceling a paid order returns the stock; delivered orders cannot be canceled")
    void ct12Cancellation() {
        Order order = checkedOutOrderWithTwoCroquettes();
        order.registerPayment(new PixPayment(), null, NO_FEE);
        assertTrue(order.cancel());
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertEquals(20, croquette.getStockQuantity());

        Order delivered = checkedOutOrderWithTwoCroquettes();
        delivered.registerPayment(new PixPayment(), null, NO_FEE);
        delivered.changeStatus(OrderStatus.PREPARING);
        delivered.changeStatus(OrderStatus.READY);
        delivered.changeStatus(OrderStatus.DELIVERED);
        assertThrows(InvalidOrderOperationException.class, delivered::cancel);
    }

    @Test
    @DisplayName("The service fee is applied and the total is fixed after payment")
    void serviceFee() {
        Order order = checkedOutOrderWithTwoCroquettes();
        assertEquals(0, new BigDecimal("13.20").compareTo(order.calculateTotal(new BigDecimal("0.10"))));
        order.registerPayment(new PixPayment(), null, new BigDecimal("0.10"));
        assertEquals(0, new BigDecimal("13.20").compareTo(order.calculateTotal(new BigDecimal("0.25"))));
    }

    @Test
    @DisplayName("A deactivated product cannot be added to an order")
    void deactivatedProduct() {
        croquette.setActive(false);
        assertFalse(croquette.isAvailable());
        assertThrows(InsufficientStockException.class, () -> new Order(ann).addItem(croquette, 1));
    }
}
