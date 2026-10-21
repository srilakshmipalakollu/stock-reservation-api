package com.example.stock_reservation_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stock_reservation_api.entity.Product;
import com.example.stock_reservation_api.repository.ProductRepository;
@Service 
public class ProductService {
    private final ProductRepository productRepository;

 // Service depends on product repository (Constructor dependency injection)
    public ProductService(ProductRepository productRepository){
        this.productRepository=productRepository;
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();

    }
    
}
