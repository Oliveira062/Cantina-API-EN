package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentException extends CanteenException {

    public InvalidPaymentException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
