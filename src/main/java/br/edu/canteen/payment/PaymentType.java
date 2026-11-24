package br.edu.canteen.payment;

import br.edu.canteen.exception.InvalidPaymentException;

import java.text.Normalizer;

public enum PaymentType {
    PIX("PIX"),
    CARD("Card"),
    CASH("Cash");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentType from(String text) {
        if (text == null || text.isBlank()) {
            throw invalid(text);
        }
        String normalized = Normalizer.normalize(text.trim().toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        for (PaymentType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw invalid(text);
    }

    private static InvalidPaymentException invalid(String text) {
        return new InvalidPaymentException(String.format(
                "Payment method \"%s\" is invalid. Choose PIX, CARD or CASH.", text == null ? "" : text));
    }
}
