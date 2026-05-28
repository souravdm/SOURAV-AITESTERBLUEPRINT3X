package com.restfulbooker.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for POST /booking.
 * Wraps the assigned bookingid and the full booking object.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBookingResponse {
    private Integer bookingid;
    private Booking booking;
}
