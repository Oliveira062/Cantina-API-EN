package br.edu.canteen.exception;

import br.edu.canteen.util.Money;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class InsufficientAmountException extends CanteenException {

    public InsufficientAmountException(BigDecimal total, BigDecimal received) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, buildMessage(total, received == null ? BigDecimal.ZERO : received));
    }

    private static String buildMessage(BigDecimal total, BigDecimal received) {
        return String.format("Amount received (%s) is less than the order total (%s). %s missing.",
                Money.format(received), Money.format(total), Money.format(total.subtract(received)));
    }
}
