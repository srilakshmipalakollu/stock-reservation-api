package com.example.stock_reservation_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name="reservation_items")
public class ReservationItem {
    @Id 
    @GeneratedValue (strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn (name="reservation_id",nullable=false)
    private Reservation reservation;
    
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn (name="product_id",nullable=false)
    private Product product;

    @Column (nullable=false)
    private int quantity;

    public ReservationItem(){

    }

    public ReservationItem(
        Reservation reservation,
        Product product,
        int quantity
    ){
        this.reservation=reservation;
        this.product=product;
        this.quantity=quantity;
    }
    public Long getId(){
        return id;
    }
    public Reservation getReservation(){
        return reservation;
    }
    public Product getProduct(){
        return product;
    }
    public int getQuantity(){
        return quantity;
    }
}
