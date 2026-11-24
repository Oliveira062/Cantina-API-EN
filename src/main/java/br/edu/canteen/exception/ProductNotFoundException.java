package br.edu.canteen.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends CanteenException {

    public ProductNotFoundException(Long code) {
        this(String.format("Product with code %d not found. Check the menu for valid codes.", code));
    }

    public ProductNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
