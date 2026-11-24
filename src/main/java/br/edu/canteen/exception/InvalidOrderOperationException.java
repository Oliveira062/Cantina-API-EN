package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class InvalidOrderOperationException extends CanteenException {

    public InvalidOrderOperationException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
