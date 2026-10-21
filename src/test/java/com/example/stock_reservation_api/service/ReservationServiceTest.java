
package com.example.stock_reservation_api.service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stock_reservation_api.dto.ReservationLineRequest;
import com.example.stock_reservation_api.dto.ReservationRequest;
import com.example.stock_reservation_api.entity.Product;
import com.example.stock_reservation_api.entity.Reservation;
import com.example.stock_reservation_api.entity.ReservationItem;
import com.example.stock_reservation_api.enums.ReservationStatus;
import com.example.stock_reservation_api.repository.ProductRepository;
import com.example.stock_reservation_api.repository.ReservationItemRepository;
import com.example.stock_reservation_api.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationItemRepository reservationItemRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createReservationWithAvailableStock() {
        Product product = new Product();
        setProductId(product, 1L);
        product.setSku("LAPTOP-001");
        product.setTotalStock(10);
        product.setReservedStock(0);

        when(productRepository.findAllByIdInForUpdate(anyList()))
                .thenAnswer(invocation -> {
                    List<Long> ids = invocation.getArgument(0);
                    return ids.contains(1L) ? List.of(product) : List.of();
                });

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationLineRequest line = new ReservationLineRequest();
        line.setProductId(1L);
        line.setQuantity(2);

        ReservationRequest request = new ReservationRequest();
        request.setItems(List.of(line));

        Reservation result = reservationService.createReservation(request);

        assertNotNull(result);
        assertEquals(2, product.getReservedStock());
        verify(reservationItemRepository).save(any());
    }

    @Test
    void createReservationWithInsufficientStock() {
        Product product = new Product();
        setProductId(product, 1L);
        product.setSku("LAPTOP-001");
        product.setTotalStock(10);
        product.setReservedStock(0);

        when(productRepository.findAllByIdInForUpdate(anyList()))
                .thenReturn(List.of(product));

        ReservationLineRequest line = new ReservationLineRequest();
        line.setProductId(1L);
        line.setQuantity(999);

        ReservationRequest request = new ReservationRequest();
        request.setItems(List.of(line));

        assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(request));

        assertEquals(0, product.getReservedStock());
        Mockito.verify(reservationRepository, Mockito.never())
                .save(any(Reservation.class));
    }

    @Test
    void createReservationWithMissingProduct() {
        when(productRepository.findAllByIdInForUpdate(anyList()))
                .thenReturn(List.of());

        ReservationLineRequest line = new ReservationLineRequest();
        line.setProductId(999L);
        line.setQuantity(1);

        ReservationRequest request = new ReservationRequest();
        request.setItems(List.of(line));

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(request));

        Mockito.verify(reservationRepository, Mockito.never())
                .save(any(Reservation.class));
    }

    
@Test
void cancelReservationReleasesStock() {
    Product product = new Product();
    setProductId(product, 1L);
    product.setSku("LAPTOP-001");
    product.setTotalStock(10);
    product.setReservedStock(2);

    Reservation reservation = new Reservation();
    setReservationId(reservation, 1L);
    reservation.setStatus(ReservationStatus.ACTIVE);

    // Use the constructor defined in ReservationItem.java
    ReservationItem item = new ReservationItem(
            reservation,
            product,
            2
    );

    when(reservationRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(reservation));

    when(reservationItemRepository.findByReservationId(1L))
            .thenReturn(List.of(item));

    reservationService.cancelReservation(1L);

    assertEquals(0, product.getReservedStock());
    assertEquals(
            ReservationStatus.CANCELLED,
            reservation.getStatus()
    );
}


    private void setProductId(Product product, Long id) {
        try {
            Field field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(product, id);
        } catch (ReflectiveOperationException e) {
            fail("Could not set product ID for test: " + e.getMessage());
        }
    }

    private void setReservationId(Reservation reservation, Long id) {
        try {
            Field field = Reservation.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(reservation, id);
        } catch (ReflectiveOperationException e) {
            fail("Could not set reservation ID for test: " + e.getMessage());
        }
    }

@Test
void confirmingReservationTwiceIsIdempotent() {
    Reservation reservation = new Reservation();
    setReservationId(reservation, 1L);
    reservation.setStatus(ReservationStatus.ACTIVE);

    when(reservationRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(reservation));

    reservationService.confirmReservation(1L);

    assertEquals(
            ReservationStatus.CONFIRMED,
            reservation.getStatus()
    );

    reservationService.confirmReservation(1L);

    assertEquals(
            ReservationStatus.CONFIRMED,
            reservation.getStatus()
    );

    verify(reservationRepository, times(2))
            .findByIdForUpdate(1L);
}


// Test repeated cancellation
@Test
void cancellingReservationTwiceDoesNotReleaseStockTwice() {
    Product product = new Product();
    setProductId(product, 1L);
    product.setSku("LAPTOP-001");
    product.setTotalStock(10);
    product.setReservedStock(2);

    Reservation reservation = new Reservation();
    setReservationId(reservation, 1L);
    reservation.setStatus(ReservationStatus.ACTIVE);

    ReservationItem item = new ReservationItem(
            reservation,
            product,
            2
    );

    when(reservationRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(reservation));

    when(reservationItemRepository.findByReservationId(1L))
            .thenReturn(List.of(item));

    // First cancellation releases 2 units.
    reservationService.cancelReservation(1L);
    assertEquals(0, product.getReservedStock());
    assertEquals(
            ReservationStatus.CANCELLED,
            reservation.getStatus()
    );

    // Second cancellation must not release stock again.
    reservationService.cancelReservation(1L);
    assertEquals(0, product.getReservedStock());

    verify(reservationItemRepository, times(1))
            .findByReservationId(1L);
}
// Test Invalid state transitions

@Test
void cancellingConfirmedReservationThrowsException() {
    Reservation reservation = new Reservation();
    setReservationId(reservation, 1L);
    reservation.setStatus(ReservationStatus.CONFIRMED);

    when(reservationRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(reservation));

    assertThrows(
            IllegalStateException.class,
            () -> reservationService.cancelReservation(1L)
    );

    assertEquals(
            ReservationStatus.CONFIRMED,
            reservation.getStatus()
    );

    verify(reservationItemRepository, times(0))
            .findByReservationId(1L);
}


// Confirming a cancelled reservation must fail

@Test
void confirmingCancelledReservationThrowsException() {
    Reservation reservation = new Reservation();
    setReservationId(reservation, 1L);
    reservation.setStatus(ReservationStatus.CANCELLED);

    when(reservationRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(reservation));

    assertThrows(
            IllegalStateException.class,
            () -> reservationService.confirmReservation(1L)
    );

    assertEquals(
            ReservationStatus.CANCELLED,
            reservation.getStatus()
    );
}


// Test a missing reservation

@Test
void confirmingNonexistentReservationThrowsException() {
    when(reservationRepository.findByIdForUpdate(999L))
            .thenReturn(Optional.empty());

    assertThrows(
            IllegalArgumentException.class,
            () -> reservationService.confirmReservation(999L)
    );

    verify(reservationRepository, times(1))
            .findByIdForUpdate(999L);
}


}
