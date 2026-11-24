package br.edu.canteen.payment;

import java.math.BigDecimal;

public record PaymentResult(boolean approved, BigDecimal change, String message) {

    public static PaymentResult approved(String message) {
        return new PaymentResult(true, BigDecimal.ZERO, message);
    }
}
