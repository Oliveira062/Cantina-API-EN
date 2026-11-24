package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class LoginBlockedException extends CanteenException {

    public LoginBlockedException(long minutesLeft) {
        super(HttpStatus.TOO_MANY_REQUESTS, String.format(
                "Too many failed login attempts. Try again in %d minute(s).", minutesLeft));
    }
}
