package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public abstract class CanteenException extends RuntimeException {

    private final HttpStatus status;

    protected CanteenException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
