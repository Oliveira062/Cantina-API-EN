package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CanteenException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }
}
