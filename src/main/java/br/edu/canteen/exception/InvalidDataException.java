package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class InvalidDataException extends CanteenException {

    public InvalidDataException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
