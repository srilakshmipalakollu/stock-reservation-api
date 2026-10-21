package com.example.stock_reservation_api.entity;

import java.time.LocalDateTime;

import com.example.stock_reservation_api.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity 
@Table(name="reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column (nullable=false)
    private ReservationStatus status;

    @Column(name="created_at",nullable=false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Reservation(){

    }

    @PrePersist
    protected void onCreate(){
        LocalDateTime now=LocalDateTime.now();
        createdAt=now;
        updatedAt=now;

        if(status==null){
            status=ReservationStatus.ACTIVE;

        }
    }

    @PreUpdate 
    protected void onUpdate(){
        updatedAt=LocalDateTime.now();
    }
    public Long getId(){
        return id;
    }
    public ReservationStatus getStatus(){
        return status;
    }
    public void setStatus(ReservationStatus status){
        this.status=status;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }

    
}
