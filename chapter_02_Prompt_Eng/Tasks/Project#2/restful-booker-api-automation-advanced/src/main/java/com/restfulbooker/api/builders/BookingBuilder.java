package com.restfulbooker.api.builders;

import com.github.javafaker.Faker;
import com.restfulbooker.api.models.Booking;
import com.restfulbooker.api.models.BookingDates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Fluent builder for {@link Booking} request payloads.
 * Provides sensible random defaults via Faker; every field can be overridden.
 */
public class BookingBuilder {

    private static final Faker faker = new Faker();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String firstname;
    private String lastname;
    private Integer totalprice;
    private Boolean depositpaid;
    private String checkin;
    private String checkout;
    private String additionalneeds;

    /** Creates a builder with randomized valid defaults. */
    public static BookingBuilder aBooking() {
        BookingBuilder b = new BookingBuilder();
        b.firstname       = faker.name().firstName();
        b.lastname        = faker.name().lastName();
        b.totalprice      = faker.number().numberBetween(50, 500);
        b.depositpaid     = faker.bool().bool();
        b.checkin         = LocalDate.now().plusDays(1).format(DATE_FMT);
        b.checkout        = LocalDate.now().plusDays(5).format(DATE_FMT);
        b.additionalneeds = faker.options().option("Breakfast", "Lunch", "Dinner", "");
        return b;
    }

    /** Creates a builder pre-filled with the standard "Jim Brown" fixture. */
    public static BookingBuilder aDefaultBooking() {
        return new BookingBuilder()
                .withFirstname("Jim")
                .withLastname("Brown")
                .withTotalprice(111)
                .withDepositpaid(true)
                .withCheckin("2026-06-01")
                .withCheckout("2026-06-05")
                .withAdditionalneeds("Breakfast");
    }

    public BookingBuilder withFirstname(String firstname) { this.firstname = firstname; return this; }
    public BookingBuilder firstname(String firstname)     { return withFirstname(firstname); }

    public BookingBuilder withLastname(String lastname)   { this.lastname = lastname; return this; }
    public BookingBuilder lastname(String lastname)        { return withLastname(lastname); }

    public BookingBuilder withTotalprice(int totalprice)  { this.totalprice = totalprice; return this; }
    public BookingBuilder totalprice(int totalprice)       { return withTotalprice(totalprice); }

    public BookingBuilder withDepositpaid(boolean depositpaid) { this.depositpaid = depositpaid; return this; }
    public BookingBuilder depositpaid(boolean depositpaid)      { return withDepositpaid(depositpaid); }

    public BookingBuilder withCheckin(String checkin)     { this.checkin = checkin; return this; }
    public BookingBuilder withCheckout(String checkout)   { this.checkout = checkout; return this; }

    public BookingBuilder withAdditionalneeds(String additionalneeds) { this.additionalneeds = additionalneeds; return this; }
    public BookingBuilder additionalneeds(String additionalneeds)      { return withAdditionalneeds(additionalneeds); }

    public BookingBuilder bookingdates(com.restfulbooker.api.models.BookingDates dates) {
        this.checkin  = dates.getCheckin();
        this.checkout = dates.getCheckout();
        return this;
    }

    /** Builds and returns the immutable {@link Booking} DTO. */
    public Booking build() {
        return Booking.builder()
                .firstname(firstname)
                .lastname(lastname)
                .totalprice(totalprice)
                .depositpaid(depositpaid)
                .bookingdates(BookingDates.builder()
                        .checkin(checkin)
                        .checkout(checkout)
                        .build())
                .additionalneeds(additionalneeds)
                .build();
    }
}
