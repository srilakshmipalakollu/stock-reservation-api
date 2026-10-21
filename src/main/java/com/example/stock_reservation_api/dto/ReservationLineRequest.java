package com.example.stock_reservation_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ReservationLineRequest {
    @NotNull (message="Product id is required")
    @Positive (message="Product id must be positive")
    private Long productId;

    @NotNull (message="Quantity is required")
    @Positive (message = "Quantity must be positive")
    private Integer quantity;

    public ReservationLineRequest(){

    }
    public Long getProductId(){
        return productId;
    }

    public void setProductId(Long productId){
        this.productId=productId;
    }
    public Integer getQuantity(){
        return quantity;
    }
    public void setQuantity(Integer quantity){
        this.quantity=quantity;
    }

    
}
