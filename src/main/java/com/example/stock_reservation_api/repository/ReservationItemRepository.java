// Manages individual reservation items

package com.example.stock_reservation_api.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock_reservation_api.entity.ReservationItem;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long>{
    List<ReservationItem> findByReservationId(Long reservationId);
}
