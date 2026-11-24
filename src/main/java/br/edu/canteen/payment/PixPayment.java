package br.edu.canteen.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PixPayment implements PaymentStrategy {

    @Override
    public PaymentResult process(BigDecimal total, BigDecimal amountReceived) {
        return PaymentResult.approved("PIX payment approved.");
    }

    @Override
    public PaymentType getType() {
        return PaymentType.PIX;
    }
}
