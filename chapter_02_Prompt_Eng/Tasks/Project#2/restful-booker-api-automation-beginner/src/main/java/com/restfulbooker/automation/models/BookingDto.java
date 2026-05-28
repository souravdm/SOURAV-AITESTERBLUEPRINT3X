package com.restfulbooker.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request / response DTO for booking payload.
 *
 * <p>Source: <a href="https://restful-booker.herokuapp.com/apidoc/index.html#api-Booking-CreateBooking">
 *     Restful Booker API Docs — CreateBooking</a>
 *
 * <pre>
 * {
 *   "firstname":      "Jim",
 *   "lastname":       "Brown",
 *   "totalprice":     111,
 *   "depositpaid":    true,
 *   "bookingdates": {
 *       "checkin":    "2018-01-01",
 *       "checkout":   "2019-01-01"
 *   },
 *   "additionalneeds": "Breakfast"
 * }
 * </pre>
 *
 * {@code additionalneeds} is optional per the spec; Jackson will omit it from
 * serialisation when null if {@code @JsonInclude} is added.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDto {

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("totalprice")
    private int totalprice;

    @JsonProperty("depositpaid")
    private boolean depositpaid;

    @JsonProperty("bookingdates")
    private BookingDates bookingdates;

    @JsonProperty("additionalneeds")
    private String additionalneeds;

    // ── Constructors ─────────────────────────────────────────────────────────

    public BookingDto() {}

    private BookingDto(Builder builder) {
        this.firstname       = builder.firstname;
        this.lastname        = builder.lastname;
        this.totalprice      = builder.totalprice;
        this.depositpaid     = builder.depositpaid;
        this.bookingdates    = builder.bookingdates;
        this.additionalneeds = builder.additionalneeds;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String       getFirstname()       { return firstname; }
    public String       getLastname()        { return lastname; }
    public int          getTotalprice()      { return totalprice; }
    public boolean      isDepositpaid()      { return depositpaid; }
    public BookingDates getBookingdates()    { return bookingdates; }
    public String       getAdditionalneeds() { return additionalneeds; }

    public void setFirstname(String firstname)             { this.firstname       = firstname; }
    public void setLastname(String lastname)               { this.lastname        = lastname; }
    public void setTotalprice(int totalprice)              { this.totalprice      = totalprice; }
    public void setDepositpaid(boolean depositpaid)        { this.depositpaid     = depositpaid; }
    public void setBookingdates(BookingDates bookingdates) { this.bookingdates    = bookingdates; }
    public void setAdditionalneeds(String additionalneeds) { this.additionalneeds = additionalneeds; }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String firstname;
        private String lastname;
        private int totalprice;
        private boolean depositpaid;
        private BookingDates bookingdates;
        private String additionalneeds;

        private Builder() {}

        public Builder firstname(String v)           { this.firstname       = v; return this; }
        public Builder lastname(String v)            { this.lastname        = v; return this; }
        public Builder totalprice(int v)             { this.totalprice      = v; return this; }
        public Builder depositpaid(boolean v)        { this.depositpaid     = v; return this; }
        public Builder bookingdates(BookingDates v)  { this.bookingdates    = v; return this; }
        public Builder additionalneeds(String v)     { this.additionalneeds = v; return this; }

        public BookingDto build() { return new BookingDto(this); }
    }
}
