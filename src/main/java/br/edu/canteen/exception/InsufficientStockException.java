package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends CanteenException {

    public InsufficientStockException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }

    public InsufficientStockException(String product, int requested, int available) {
        this(String.format("Not enough stock for %s: requested %d, available %d. Reduce the quantity.",
                product, requested, available));
    }
}
