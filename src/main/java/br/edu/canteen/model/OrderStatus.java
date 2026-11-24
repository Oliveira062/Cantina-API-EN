package br.edu.canteen.model;

public enum OrderStatus {
    CREATED("Created"),
    AWAITING_PAYMENT("Awaiting payment"),
    PAID("Paid"),
    PREPARING("Preparing"),
    READY("Ready"),
    DELIVERED("Delivered"),
    CANCELED("Canceled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case CREATED -> next == AWAITING_PAYMENT || next == CANCELED;
            case AWAITING_PAYMENT -> next == PAID || next == CANCELED;
            case PAID -> next == PREPARING || next == CANCELED;
            case PREPARING -> next == READY || next == CANCELED;
            case READY -> next == DELIVERED || next == CANCELED;
            case DELIVERED, CANCELED -> false;
        };
    }
}
