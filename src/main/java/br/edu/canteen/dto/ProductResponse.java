package br.edu.canteen.dto;

import br.edu.canteen.model.Product;

import java.math.BigDecimal;

public record ProductResponse(Long code, String name, String description, BigDecimal price, int stockQuantity,
                              boolean available, boolean active) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getStockQuantity(), product.isAvailable(), product.isActive());
    }
}
