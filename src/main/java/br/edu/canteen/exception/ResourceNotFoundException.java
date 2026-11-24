package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CanteenException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
