package br.edu.canteen.model;

public enum Role {
    CUSTOMER("Customer"),
    CASHIER("Cashier"),
    KITCHEN("Kitchen"),
    ADMIN("Administrator");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStaff() {
        return this != CUSTOMER;
    }
}
