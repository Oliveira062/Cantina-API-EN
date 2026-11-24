package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends CanteenException {

    public OrderNotFoundException(Long number) {
        super(HttpStatus.NOT_FOUND, String.format("Order #%d not found. Check the number.", number));
    }
}
