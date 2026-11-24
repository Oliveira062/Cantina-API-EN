package br.edu.canteen.payment;

import br.edu.canteen.exception.InsufficientAmountException;
import br.edu.canteen.util.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CashPayment implements PaymentStrategy {

    @Override
    public PaymentResult process(BigDecimal total, BigDecimal amountReceived) {
        if (amountReceived == null || amountReceived.compareTo(total) < 0) {
            throw new InsufficientAmountException(total, amountReceived);
        }
        BigDecimal change = amountReceived.subtract(total);
        return new PaymentResult(true, change,
                "Cash payment approved. Change: " + Money.format(change) + ".");
    }

    @Override
    public PaymentType getType() {
        return PaymentType.CASH;
    }
}
