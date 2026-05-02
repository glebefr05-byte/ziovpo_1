package com.licensing.service;

import com.licensing.entities.Product;
import com.licensing.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product getProductOrFail(UUID productId) throws Exception {
        return productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found: " + productId));
    }
}