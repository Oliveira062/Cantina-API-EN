package br.edu.canteen.service;

import br.edu.canteen.dto.UpdateProductRequest;
import br.edu.canteen.dto.ProductRequest;
import br.edu.canteen.dto.ProductResponse;
import br.edu.canteen.exception.ProductNotFoundException;
import br.edu.canteen.model.Product;
import br.edu.canteen.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listMenu(boolean includeInactive) {
        List<Product> products = includeInactive
                ? repository.findAllByOrderByNameAsc()
                : repository.findByActiveTrueOrderByNameAsc();
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long code, boolean includeInactive) {
        Product product = find(code);
        if (!product.isActive() && !includeInactive) {
            throw new ProductNotFoundException(code);
        }
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest data) {
        Product product = new Product(data.name(), data.description(), data.price(), data.initialQuantity());
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse updateDetails(Long code, UpdateProductRequest data) {
        Product product = find(code);
        product.updateDetails(data.name(), data.description());
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updatePrice(Long code, BigDecimal newPrice) {
        Product product = find(code);
        product.updatePrice(newPrice);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse restock(Long code, int quantity) {
        Product product = find(code);
        product.restock(quantity);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse setActive(Long code, boolean active) {
        Product product = find(code);
        product.setActive(active);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public Product find(Long code) {
        return repository.findById(code).orElseThrow(() -> new ProductNotFoundException(code));
    }
}
