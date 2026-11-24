package br.edu.canteen.payment;

import br.edu.canteen.exception.InvalidPaymentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentFactoryTest {

    private final PaymentFactory factory =
            new PaymentFactory(List.of(new PixPayment(), new CardPayment(), new CashPayment()));

    @Test
    @DisplayName("CT09 - an unknown payment method is rejected")
    void ct09UnknownPayment() {
        assertThrows(InvalidPaymentException.class, () -> factory.create("BOLETO"));
        assertThrows(InvalidPaymentException.class, () -> factory.create(" "));
    }

    @Test
    @DisplayName("Accepts the type in lowercase and with extra spaces")
    void acceptsSpellingVariations() {
        assertInstanceOf(CardPayment.class, factory.create("card"));
        assertInstanceOf(PixPayment.class, factory.create("pix"));
        assertInstanceOf(CashPayment.class, factory.create(" Cash "));
    }
}
