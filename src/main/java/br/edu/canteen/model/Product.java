package br.edu.canteen.model;

import br.edu.canteen.exception.InvalidDataException;
import br.edu.canteen.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;

    protected Product() {
    }

    public Product(String name, String description, BigDecimal price, int initialQuantity) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("The product name is required.");
        }
        validatePrice(price);
        if (initialQuantity < 0) {
            throw new InvalidDataException("The initial quantity cannot be negative.");
        }
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
        this.price = price;
        this.stockQuantity = initialQuantity;
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("The price must be greater than zero.");
        }
    }

    public boolean isAvailable() {
        return active && stockQuantity > 0;
    }

    public boolean hasStock(int quantity) {
        return quantity > 0 && quantity <= stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new InsufficientStockException(name, quantity, stockQuantity);
        }
        stockQuantity -= quantity;
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidDataException("The restock quantity must be greater than zero.");
        }
        stockQuantity += quantity;
    }

    void returnStock(int quantity) {
        stockQuantity += quantity;
    }

    public void updatePrice(BigDecimal newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    public void updateDetails(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("The product name is required.");
        }
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
    }
}
