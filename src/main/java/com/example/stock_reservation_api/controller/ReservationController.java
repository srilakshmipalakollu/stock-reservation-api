
package com.example.stock_reservation_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock_reservation_api.dto.ReservationRequest;
import com.example.stock_reservation_api.entity.Reservation;
import com.example.stock_reservation_api.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation createReservation(
            @Valid @RequestBody ReservationRequest request) {
        return reservationService.createReservation(request);
    }

@PutMapping("/{reservationId}/cancel")
public Reservation cancelReservation(
        @PathVariable Long reservationId) {
    return reservationService.cancelReservation(reservationId);
}

@PutMapping("/{reservationId}/confirm")
public Reservation confirmReservation(
        @PathVariable Long reservationId) {

    System.out.println("Confirm endpoint called for ID: " + reservationId);

    return reservationService.confirmReservation(reservationId);
}
@GetMapping("/{reservationId}")
public Reservation getReservation(
        @PathVariable Long reservationId) {
    return reservationService.getReservation(reservationId);
}

}
