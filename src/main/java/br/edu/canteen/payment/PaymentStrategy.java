package br.edu.canteen.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {

    PaymentResult process(BigDecimal total, BigDecimal amountReceived);

    PaymentType getType();
}
