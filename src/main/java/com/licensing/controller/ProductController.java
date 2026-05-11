package com.licensing.controller;

import com.licensing.entities.Product;
import com.licensing.model.ProductDto;
import com.licensing.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setIsBlocked(product.isBlocked());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id));
        return ResponseEntity.ok(convertToDto(product));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ProductDto> getProductByName(@PathVariable String name) {
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with name: " + name));
        return ResponseEntity.ok(convertToDto(product));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductDto>> getActiveProducts() {
        List<ProductDto> products = productRepository.findAll().stream()
                .filter(product -> !product.isBlocked())
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        if (productRepository.existsByName(productDto.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product with name " + productDto.getName() + " already exists");
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBlocked(productDto.getIsBlocked() != null ? productDto.getIsBlocked() : false);

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedProduct));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable UUID id,
                                                    @Valid @RequestBody ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id));

        if (!product.getName().equals(productDto.getName()) &&
                productRepository.existsByName(productDto.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product with name " + productDto.getName() + " already exists");
        }

        product.setName(productDto.getName());
        if (productDto.getIsBlocked() != null) {
            product.setBlocked(productDto.getIsBlocked());
        }

        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDto(updatedProduct));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "message", "Product deleted successfully",
                "id", id.toString()
        ));
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> blockProduct(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id));

        product.setBlocked(true);
        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDto(updatedProduct));
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> unblockProduct(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id));

        product.setBlocked(false);
        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(convertToDto(updatedProduct));
    }
}