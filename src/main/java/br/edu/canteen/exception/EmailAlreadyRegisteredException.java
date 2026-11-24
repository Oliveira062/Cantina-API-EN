package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends CanteenException {

    public EmailAlreadyRegisteredException() {
        super(HttpStatus.CONFLICT, "An account with this email already exists.");
    }
}
