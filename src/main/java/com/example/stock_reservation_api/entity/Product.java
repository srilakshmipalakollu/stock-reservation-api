package com.example.stock_reservation_api.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name="products")
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column (nullable=false,unique=true)
    private String sku;

    @Column (nullable=false)
    private String name;

    @Column (name="total_stock",nullable=false)
    private int totalStock;

    @Column (name="reserved_stock",nullable=false)
    private int reservedStock;

    public Product(){

    }

    public Long getId(){
        return id;
    }

    public String getSku(){
        return sku;
    }
    public void setSku(String sku){
        this.sku=sku;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public int getTotalStock(){
        return totalStock;
    }
    public void setTotalStock(int totalStock){
        this.totalStock=totalStock;
    }
    public int getReservedStock(){
        return reservedStock;
    }
    public void setReseverdStock(int reservedStock){
        this.reservedStock=reservedStock;
    }
    public void setReservedStock(int reservedStock) {
    this.reservedStock = reservedStock;
}
    public int getAvailableStock(){
        return totalStock-reservedStock;
    }
}
