package com.restfulbooker.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Nested DTO for the {@code bookingdates} object.
 *
 * <p>Source: <a href="https://restful-booker.herokuapp.com/apidoc/index.html#api-Booking-CreateBooking">
 *     Restful Booker API Docs — CreateBooking</a>
 *
 * <pre>
 * {
 *   "checkin":  "2018-01-01",
 *   "checkout": "2019-01-01"
 * }
 * </pre>
 *
 * Used by {@link BookingDto}.  Not used by /ping tests; included so the
 * framework is immediately extensible to booking endpoint tests without
 * refactoring.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDates {

    @JsonProperty("checkin")
    private String checkin;

    @JsonProperty("checkout")
    private String checkout;

    // ── Constructors ─────────────────────────────────────────────────────────

    public BookingDates() {}

    private BookingDates(Builder builder) {
        this.checkin  = builder.checkin;
        this.checkout = builder.checkout;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getCheckin()  { return checkin; }
    public String getCheckout() { return checkout; }

    public void setCheckin(String checkin)   { this.checkin  = checkin; }
    public void setCheckout(String checkout) { this.checkout = checkout; }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String checkin;
        private String checkout;

        private Builder() {}

        public Builder checkin(String checkin)   { this.checkin  = checkin;  return this; }
        public Builder checkout(String checkout) { this.checkout = checkout; return this; }

        public BookingDates build() { return new BookingDates(this); }
    }

    @Override
    public String toString() {
        return "BookingDates{checkin='" + checkin + "', checkout='" + checkout + "'}";
    }
}
