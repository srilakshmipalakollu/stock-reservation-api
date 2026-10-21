
package com.example.stock_reservation_api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock_reservation_api.dto.ReservationLineRequest;
import com.example.stock_reservation_api.dto.ReservationRequest;
import com.example.stock_reservation_api.entity.Product;
import com.example.stock_reservation_api.entity.Reservation;
import com.example.stock_reservation_api.entity.ReservationItem;
import com.example.stock_reservation_api.enums.ReservationStatus;
import com.example.stock_reservation_api.repository.ProductRepository;
import com.example.stock_reservation_api.repository.ReservationItemRepository;
import com.example.stock_reservation_api.repository.ReservationRepository;

@Service
public class ReservationService {

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;

    public ReservationService(
            ProductRepository productRepository,
            ReservationRepository reservationRepository,
            ReservationItemRepository reservationItemRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.reservationItemRepository = reservationItemRepository;
    }

    @Transactional
    public Reservation createReservation(ReservationRequest request) {

        List<ReservationLineRequest> items = request.getItems();

        Set<Long> productIds = new HashSet<>();
        for (ReservationLineRequest item : items) {
            if (!productIds.add(item.getProductId())) {
                throw new IllegalArgumentException(
                        "Duplicate product IDs are not allowed");
            }
        }

        List<Long> ids = new ArrayList<>(productIds);
        ids.sort(Long::compareTo);

        List<Product> products =
                productRepository.findAllByIdInForUpdate(ids);

        if (products.size() != ids.size()) {
            throw new IllegalArgumentException(
                    "One or more products do not exist");
        }

        Map<Long, Product> productsById = new HashMap<>();
        for (Product product : products) {
            productsById.put(product.getId(), product);
        }

        for (ReservationLineRequest item : items) {
            Product product = productsById.get(item.getProductId());

            if (product.getAvailableStock() < item.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for product: " + product.getSku());
            }
        }

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);

        for (ReservationLineRequest item : items) {
            Product product = productsById.get(item.getProductId());

            product.setReservedStock(
                    product.getReservedStock() + item.getQuantity());

            ReservationItem reservationItem = new ReservationItem(
                    reservation, product, item.getQuantity());

            reservationItemRepository.save(reservationItem);
        }

        return reservation;
    }


@Transactional
public Reservation cancelReservation(Long reservationId) {
Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
        .orElseThrow(() ->
                new IllegalArgumentException("Reservation not found"));

    // Repeated cancellation must not release stock again.
    if (reservation.getStatus() == ReservationStatus.CANCELLED) {
        return reservation;
    }

    // Only active reservations can be cancelled.
    if (reservation.getStatus() != ReservationStatus.ACTIVE) {
        throw new IllegalStateException(
                "Only active reservations can be cancelled");
    }

    List<ReservationItem> items =
            reservationItemRepository.findByReservationId(reservationId);

    for (ReservationItem item : items) {
        Product product = item.getProduct();

        product.setReservedStock(
                product.getReservedStock() - item.getQuantity());
    }

    reservation.setStatus(ReservationStatus.CANCELLED);

    return reservation;
}


@Transactional
public Reservation confirmReservation(Long reservationId) {
   Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
        .orElseThrow(() ->
                new IllegalArgumentException("Reservation not found"));

    // Repeated confirmation is idempotent.
    if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
        return reservation;
    }

    if (reservation.getStatus() != ReservationStatus.ACTIVE) {
        throw new IllegalStateException(
                "Only active reservations can be confirmed");
    }

    reservation.setStatus(ReservationStatus.CONFIRMED);

    return reservation;
}

@Transactional(readOnly = true)
public Reservation getReservation(Long reservationId) {
    return reservationRepository.findById(reservationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Reservation not found"));
}
}
