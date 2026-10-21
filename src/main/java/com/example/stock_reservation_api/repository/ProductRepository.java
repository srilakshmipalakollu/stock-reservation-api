package com.example.stock_reservation_api.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stock_reservation_api.entity.Product;

import jakarta.persistence.LockModeType;

public interface  ProductRepository extends JpaRepository<Product, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query ("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
    List<Product> findAllByIdInForUpdate(@Param("ids")List<Long> ids);
    
}
