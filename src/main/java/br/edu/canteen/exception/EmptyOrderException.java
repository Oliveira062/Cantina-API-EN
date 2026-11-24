package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class EmptyOrderException extends CanteenException {

    public EmptyOrderException(Long orderNumber) {
        super(HttpStatus.UNPROCESSABLE_CONTENT,
                String.format("Order #%d has no items. Add at least one product before checking out.", orderNumber));
    }
}
