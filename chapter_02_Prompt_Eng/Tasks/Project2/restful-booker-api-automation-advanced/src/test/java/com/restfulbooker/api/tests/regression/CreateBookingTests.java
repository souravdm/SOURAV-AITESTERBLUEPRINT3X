package com.restfulbooker.api.tests.regression;

import com.restfulbooker.api.builders.BookingBuilder;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.Booking;
import com.restfulbooker.api.models.BookingDates;
import com.restfulbooker.api.models.CreateBookingResponse;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import com.restfulbooker.api.utils.DateUtils;
import com.restfulbooker.api.utils.FakerUtils;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Regression tests for POST /booking.
 * Test Plan scenarios: TS-CB-001 through TS-CB-016
 */
public class CreateBookingTests extends BaseTest {

    private final BookingClient bookingClient = new BookingClient();

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_CB_001 | TS-CB-001 — POST /booking with valid payload returns 200 and bookingid"
    )
    public void tc_cb_001_validPayloadReturns200WithId() {
        Booking booking = BookingBuilder.aBooking().build();
        Response resp = bookingClient.createBooking(booking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        CreateBookingResponse created = resp.as(CreateBookingResponse.class);
        Assertions.assertThat(created.getBookingid())
                .as("bookingid must be a positive integer")
                .isGreaterThan(0);
        AssertionHelper.assertResponseTimeSla(resp);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_CB_002 | TS-CB-002 — POST /booking response body contains booking object matching input"
    )
    public void tc_cb_002_responseBodyMatchesInput() {
        Booking booking = BookingBuilder.aDefaultBooking().build();
        Response resp = bookingClient.createBooking(booking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        CreateBookingResponse created = resp.as(CreateBookingResponse.class);
        Booking returned = created.getBooking();
        Assertions.assertThat(returned.getFirstname()).isEqualTo(booking.getFirstname());
        Assertions.assertThat(returned.getLastname()).isEqualTo(booking.getLastname());
        Assertions.assertThat(returned.getTotalprice()).isEqualTo(booking.getTotalprice());
        Assertions.assertThat(returned.getDepositpaid()).isEqualTo(booking.getDepositpaid());
    }

    @Test(
        groups  = {"regression", "booking", "schema"},
        description = "TC_CB_003 | TS-CB-003 — POST /booking response validates against create-booking-response schema"
    )
    public void tc_cb_003_schemaValidation() {
        Booking booking = BookingBuilder.aBooking().build();
        Response resp = bookingClient.createBooking(booking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/create-booking-response-schema.json");
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_CB_004 | TS-CB-004 — POST /booking with depositpaid=false is persisted correctly"
    )
    public void tc_cb_004_depositPaidFalse() {
        Booking booking = BookingBuilder.aBooking().depositpaid(false).build();
        Response resp = bookingClient.createBooking(booking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        boolean dp = resp.jsonPath().getBoolean("booking.depositpaid");
        Assertions.assertThat(dp).as("depositpaid should be false").isFalse();
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_CB_005 | TS-CB-005 — POST /booking with additionalneeds returns it in response"
    )
    public void tc_cb_005_additionalNeedsRoundTrip() {
        String needs = "Gluten-free breakfast";
        Booking booking = BookingBuilder.aBooking().additionalneeds(needs).build();
        Response resp = bookingClient.createBooking(booking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        String returned = resp.jsonPath().getString("booking.additionalneeds");
        Assertions.assertThat(returned).isEqualTo(needs);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_006 | TS-CB-006 — POST /booking missing firstname returns 500 or 400"
    )
    public void tc_cb_006_missingFirstname() {
        Map<String, Object> body = buildValidBodyMap();
        body.remove("firstname");
        Response resp = bookingClient.createBookingRaw(body);
        Assertions.assertThat(resp.statusCode())
                .as("Missing firstname should return 4xx or 500")
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_007 | TS-CB-007 — POST /booking missing lastname returns 500 or 400"
    )
    public void tc_cb_007_missingLastname() {
        Map<String, Object> body = buildValidBodyMap();
        body.remove("lastname");
        Response resp = bookingClient.createBookingRaw(body);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_008 | TS-CB-008 — POST /booking missing totalprice returns 500 or 400"
    )
    public void tc_cb_008_missingTotalprice() {
        Map<String, Object> body = buildValidBodyMap();
        body.remove("totalprice");
        Response resp = bookingClient.createBookingRaw(body);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_009 | TS-CB-009 — POST /booking missing bookingdates returns 500 or 400"
    )
    public void tc_cb_009_missingBookingDates() {
        Map<String, Object> body = buildValidBodyMap();
        body.remove("bookingdates");
        Response resp = bookingClient.createBookingRaw(body);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_010 | TS-CB-010 — POST /booking with empty body returns 500 or 400"
    )
    public void tc_cb_010_emptyBody() {
        Response resp = bookingClient.createBookingRaw(Collections.emptyMap());
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_011 | TS-CB-011 — POST /booking totalprice as string returns 500 or 400"
    )
    public void tc_cb_011_totalpriceAsString() {
        Map<String, Object> body = buildValidBodyMap();
        body.put("totalprice", "not-a-number");
        Response resp = bookingClient.createBookingRaw(body);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "booking", "boundary"},
        description = "TC_CB_012 | TS-CB-012 — POST /booking with totalprice=0 is accepted or rejected"
    )
    public void tc_cb_012_totalpriceZero() {
        Booking booking = BookingBuilder.aBooking().totalprice(0).build();
        Response resp = bookingClient.createBooking(booking);
        Assertions.assertThat(resp.statusCode())
                .as("totalprice=0 may be accepted (200) or rejected (400)")
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "booking", "boundary"},
        description = "TC_CB_013 | TS-CB-013 — POST /booking with very large totalprice is accepted"
    )
    public void tc_cb_013_totalpriceMaxInt() {
        Booking booking = BookingBuilder.aBooking().totalprice(Integer.MAX_VALUE).build();
        Response resp = bookingClient.createBooking(booking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
    }

    @Test(
        groups  = {"regression", "booking", "boundary"},
        description = "TC_CB_014 | TS-CB-014 — POST /booking with very long firstname (255 chars)"
    )
    public void tc_cb_014_veryLongFirstname() {
        String longName = FakerUtils.stringOfLength(255);
        Booking booking = BookingBuilder.aBooking().firstname(longName).build();
        Response resp = bookingClient.createBooking(booking);
        Assertions.assertThat(resp.statusCode())
                .as("255-char firstname: 200 accepted or 400 rejected")
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_015 | TS-CB-015 — POST /booking checkout before checkin is rejected or accepted"
    )
    public void tc_cb_015_checkoutBeforeCheckin() {
        BookingDates badDates = BookingDates.builder()
                .checkin("2026-06-10").checkout("2026-06-01").build();
        Booking booking = BookingBuilder.aBooking().bookingdates(badDates).build();
        Response resp = bookingClient.createBooking(booking);
        Assertions.assertThat(resp.statusCode())
                .as("checkout before checkin: API may accept (200) or reject (400)")
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_CB_016 | TS-CB-016 — POST /booking with invalid date format returns 500 or 400"
    )
    public void tc_cb_016_invalidDateFormat() {
        BookingDates badDates = BookingDates.builder()
                .checkin(DateUtils.invalidDate()).checkout("99/99/9999").build();
        Booking booking = BookingBuilder.aBooking().bookingdates(badDates).build();
        Response resp = bookingClient.createBooking(booking);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Map<String, Object> buildValidBodyMap() {
        Map<String, Object> dates = new HashMap<>();
        dates.put("checkin", DateUtils.daysFromNow(1));
        dates.put("checkout", DateUtils.daysFromNow(5));
        Map<String, Object> body = new HashMap<>();
        body.put("firstname", FakerUtils.firstName());
        body.put("lastname", FakerUtils.lastName());
        body.put("totalprice", 150);
        body.put("depositpaid", true);
        body.put("bookingdates", dates);
        body.put("additionalneeds", "None");
        return body;
    }
}
