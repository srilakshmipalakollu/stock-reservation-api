
package com.example.stock_reservation_api;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.stock_reservation_api.dto.ReservationLineRequest;
import com.example.stock_reservation_api.dto.ReservationRequest;
import com.example.stock_reservation_api.entity.Product;
import com.example.stock_reservation_api.entity.Reservation;
import com.example.stock_reservation_api.enums.ReservationStatus;
import com.example.stock_reservation_api.repository.ProductRepository;
import com.example.stock_reservation_api.repository.ReservationRepository;
import com.example.stock_reservation_api.service.ReservationService;

@SpringBootTest

class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void shouldSaveAndRetrieveProductFromDatabase() {
        Product product = new Product();
        product.setSku("TEST-LAPTOP-001");
        product.setName("Test Laptop");
        product.setTotalStock(10);
        product.setReservedStock(0);

        Product savedProduct = productRepository.save(product);

        assertNotNull(savedProduct.getId());

        Product retrievedProduct = productRepository
                .findById(savedProduct.getId())
                .orElseThrow();

        assertEquals("TEST-LAPTOP-001", retrievedProduct.getSku());
        assertEquals("Test Laptop", retrievedProduct.getName());
        assertEquals(10, retrievedProduct.getTotalStock());
        assertEquals(0, retrievedProduct.getReservedStock());
    }

    @Test
    void shouldCreateReservationAndUpdateStockInDatabase() {
        Product product = new Product();
        product.setSku("TEST-RESERVATION-001");
        product.setName("Test Reservation Product");
        product.setTotalStock(10);
        product.setReservedStock(0);

        Product savedProduct = productRepository.saveAndFlush(product);

        ReservationLineRequest line = new ReservationLineRequest();
        line.setProductId(savedProduct.getId());
        line.setQuantity(3);

        ReservationRequest request = new ReservationRequest();
        request.setItems(List.of(line));

        Reservation reservation = reservationService.createReservation(request);

        assertNotNull(reservation.getId());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());

        Product updatedProduct = productRepository
                .findById(savedProduct.getId())
                .orElseThrow();

        assertEquals(3, updatedProduct.getReservedStock());
        assertEquals(7, updatedProduct.getAvailableStock());

        assertEquals(
                true,
                reservationRepository.existsById(reservation.getId())
        );
    }
    @Test
void shouldReleaseStockWhenReservationIsCancelled() {
    // Create a product with 10 units
    Product product = new Product();
    product.setSku("TEST-CANCEL-001");
    product.setName("Test Cancellation Product");
    product.setTotalStock(10);
    product.setReservedStock(0);

    Product savedProduct = productRepository.saveAndFlush(product);

    // Reserve 3 units
    ReservationLineRequest line = new ReservationLineRequest();
    line.setProductId(savedProduct.getId());
    line.setQuantity(3);

    ReservationRequest request = new ReservationRequest();
    request.setItems(List.of(line));

    Reservation reservation = reservationService.createReservation(request);

    // Cancel the reservation
    reservationService.cancelReservation(reservation.getId());

    // Verify stock was released
    Product updatedProduct =
            productRepository.findById(savedProduct.getId()).orElseThrow();

    assertEquals(0, updatedProduct.getReservedStock());
    assertEquals(10, updatedProduct.getAvailableStock());

    // Verify reservation status
    Reservation updatedReservation =
            reservationRepository.findById(reservation.getId()).orElseThrow();

    assertEquals(ReservationStatus.CANCELLED, updatedReservation.getStatus());
}
@Test
void shouldNotChangeAnyStockWhenReservationFails() {
    // Product 1 has enough stock
    Product laptop = new Product();
    laptop.setSku("TEST-ATOMIC-LAPTOP");
    laptop.setName("Test Laptop");
    laptop.setTotalStock(10);
    laptop.setReservedStock(0);
    Product savedLaptop = productRepository.saveAndFlush(laptop);

    // Product 2 does not have enough stock for the request
    Product mouse = new Product();
    mouse.setSku("TEST-ATOMIC-MOUSE");
    mouse.setName("Test Mouse");
    mouse.setTotalStock(2);
    mouse.setReservedStock(0);
    Product savedMouse = productRepository.saveAndFlush(mouse);

    ReservationLineRequest laptopLine = new ReservationLineRequest();
    laptopLine.setProductId(savedLaptop.getId());
    laptopLine.setQuantity(3);

    ReservationLineRequest mouseLine = new ReservationLineRequest();
    mouseLine.setProductId(savedMouse.getId());
    mouseLine.setQuantity(5);

    ReservationRequest request = new ReservationRequest();
    request.setItems(List.of(laptopLine, mouseLine));

    // The whole reservation should fail
    assertThrows(
            IllegalStateException.class,
            () -> reservationService.createReservation(request)
    );

    // Neither product's stock should change
    Product laptopAfter =
            productRepository.findById(savedLaptop.getId()).orElseThrow();
    Product mouseAfter =
            productRepository.findById(savedMouse.getId()).orElseThrow();

    assertEquals(0, laptopAfter.getReservedStock());
    assertEquals(0, mouseAfter.getReservedStock());
}
@Test
void shouldNotReserveMoreStockThanAvailableConcurrently() throws Exception {
    Product product = new Product();
    product.setSku("TEST-CONCURRENT-001");
    product.setName("Concurrency Test Product");
    product.setTotalStock(5);
    product.setReservedStock(0);

    Product savedProduct = productRepository.saveAndFlush(product);

    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    AtomicInteger successes = new AtomicInteger(0);

    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
        Future<?> first = executor.submit(() -> {
            try {
                ready.countDown();
                start.await();

                ReservationRequest request = createRequest(
                        savedProduct.getId(), 4);

                reservationService.createReservation(request);
                successes.incrementAndGet();
            } catch (IllegalStateException e) {
                // Expected if the other request reserves stock first.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        Future<?> second = executor.submit(() -> {
            try {
                ready.countDown();
                start.await();

                ReservationRequest request = createRequest(
                        savedProduct.getId(), 4);

                reservationService.createReservation(request);
                successes.incrementAndGet();
            } catch (IllegalStateException e) {
                // Expected if the other request reserves stock first.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        first.get(15, TimeUnit.SECONDS);
        second.get(15, TimeUnit.SECONDS);

        Product updated = productRepository.findById(savedProduct.getId())
                .orElseThrow();

        assertEquals(1, successes.get());
        assertEquals(4, updated.getReservedStock());
        assertTrue(updated.getReservedStock() <= updated.getTotalStock());
    } finally {
        start.countDown();
        executor.shutdownNow();
    }
}

private ReservationRequest createRequest(Long productId, int quantity) {
    ReservationLineRequest line = new ReservationLineRequest();
    line.setProductId(productId);
    line.setQuantity(quantity);

    ReservationRequest request = new ReservationRequest();
    request.setItems(List.of(line));

    return request;
}
}
