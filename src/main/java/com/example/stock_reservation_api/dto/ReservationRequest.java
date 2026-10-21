
package com.example.stock_reservation_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReservationRequest {

    @NotEmpty(message = "Reservation must contain at least one item")
    @Valid
    private List<ReservationLineRequest> items;

    public ReservationRequest() {
    }

    public List<ReservationLineRequest> getItems() {
        return items;
    }

    public void setItems(List<ReservationLineRequest> items) {
        this.items = items;
    }
}
