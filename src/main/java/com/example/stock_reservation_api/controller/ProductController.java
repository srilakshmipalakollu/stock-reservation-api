package com.example.stock_reservation_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_reservation_api.entity.Product;
import com.example.stock_reservation_api.service.ProductService;;

@RestController 
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService=productService;

    }
    @GetMapping
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }
    
}
