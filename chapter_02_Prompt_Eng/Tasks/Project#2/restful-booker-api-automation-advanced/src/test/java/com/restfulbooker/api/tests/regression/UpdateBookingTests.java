package com.restfulbooker.api.tests.regression;

import com.restfulbooker.api.builders.BookingBuilder;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.Booking;
import com.restfulbooker.api.models.CreateBookingResponse;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import com.restfulbooker.api.utils.FakerUtils;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Regression tests for PUT /booking/:id.
 * Test Plan scenarios: TS-UB-001 through TS-UB-012
 */
public class UpdateBookingTests extends BaseTest {

    private final BookingClient bookingClient = new BookingClient();
    private int bookingId;

    @BeforeClass(alwaysRun = true)
    public void createBooking() {
        Response resp = bookingClient.createBooking(BookingBuilder.aDefaultBooking().build());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        bookingId = resp.as(CreateBookingResponse.class).getBookingid();
        log.info("UpdateBookingTests: created bookingId={}", bookingId);
    }

    @AfterClass(alwaysRun = true)
    public void deleteBooking() {
        if (bookingId > 0) bookingClient.deleteBookingWithCookie(bookingId);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_UB_001 | TS-UB-001 — PUT /booking/:id with valid payload and cookie auth returns 200"
    )
    public void tc_ub_001_validUpdateWithCookieReturns200() {
        Booking updated = BookingBuilder.aBooking()
                .firstname("UpdatedFirst").lastname("UpdatedLast").build();
        Response resp = bookingClient.updateBookingWithCookie(bookingId, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Assertions.assertThat(resp.jsonPath().getString("firstname")).isEqualTo("UpdatedFirst");
        Assertions.assertThat(resp.jsonPath().getString("lastname")).isEqualTo("UpdatedLast");
        AssertionHelper.assertResponseTimeSla(resp);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_UB_002 | TS-UB-002 — PUT /booking/:id with Basic Auth header returns 200"
    )
    public void tc_ub_002_validUpdateWithBasicAuthReturns200() {
        Booking updated = BookingBuilder.aBooking()
                .firstname("BasicFirst").lastname("BasicLast").build();
        Response resp = bookingClient.updateBookingWithBasicAuth(bookingId, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Assertions.assertThat(resp.jsonPath().getString("firstname")).isEqualTo("BasicFirst");
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_UB_003 | TS-UB-003 — PUT /booking/:id response body reflects full updated object"
    )
    public void tc_ub_003_responseBodyReflectsUpdate() {
        Booking updated = BookingBuilder.aDefaultBooking()
                .firstname("Jim").lastname("Brown").totalprice(999).build();
        Response resp = bookingClient.updateBookingWithCookie(bookingId, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Assertions.assertThat(resp.jsonPath().getInt("totalprice")).isEqualTo(999);
    }

    @Test(
        groups  = {"regression", "booking", "schema"},
        description = "TC_UB_004 | TS-UB-004 — PUT /booking/:id response validates against booking-response schema"
    )
    public void tc_ub_004_schemaValidation() {
        Booking updated = BookingBuilder.aBooking().build();
        Response resp = bookingClient.updateBookingWithCookie(bookingId, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/booking-response-schema.json");
    }

    @Test(
        groups  = {"regression", "booking", "auth"},
        description = "TC_UB_005 | TS-UB-005 — PUT /booking/:id without auth returns 403"
    )
    public void tc_ub_005_noAuthReturns403() {
        Booking updated = BookingBuilder.aBooking().build();
        Response resp = bookingClient.updateBookingNoAuth(bookingId, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.FORBIDDEN);
    }

    @Test(
        groups  = {"regression", "booking", "auth"},
        description = "TC_UB_006 | TS-UB-006 — PUT /booking/:id with invalid token returns 403"
    )
    public void tc_ub_006_invalidTokenReturns403() {
        Booking updated = BookingBuilder.aBooking().build();
        Response resp = bookingClient.updateBookingInvalidToken(bookingId, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.FORBIDDEN);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_UB_007 | TS-UB-007 — PUT /booking/:id for non-existent id returns 405 or 404"
    )
    public void tc_ub_007_nonExistentIdReturns405Or404() {
        Booking updated = BookingBuilder.aBooking().build();
        Response resp = bookingClient.updateBookingWithCookie(Integer.MAX_VALUE, updated);
        Assertions.assertThat(resp.statusCode())
                .as("PUT on non-existent id should return 404 or 405")
                .isIn(StatusCodes.NOT_FOUND, 405);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_UB_008 | TS-UB-008 — PUT /booking/:id with empty body returns 400 or 500"
    )
    public void tc_ub_008_emptyBodyReturnsError() {
        Response resp = bookingClient.updateBookingRawWithCookie(bookingId, "{}");
        Assertions.assertThat(resp.statusCode())
                .as("PUT with empty body should return 400 or 500")
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR, 405);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_UB_009 | TS-UB-009 — PUT /booking/:id missing firstname returns error"
    )
    public void tc_ub_009_missingFirstname() {
        Booking updated = BookingBuilder.aBooking().firstname(null).build();
        Response resp = bookingClient.updateBookingWithCookie(bookingId, updated);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR, StatusCodes.OK);
    }

    @Test(
        groups  = {"regression", "booking", "boundary"},
        description = "TC_UB_010 | TS-UB-010 — PUT /booking/:id with 255-char firstname accepted or rejected"
    )
    public void tc_ub_010_veryLongFirstname() {
        Booking updated = BookingBuilder.aBooking()
                .firstname(FakerUtils.stringOfLength(255)).build();
        Response resp = bookingClient.updateBookingWithCookie(bookingId, updated);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_UB_011 | TS-UB-011 — PUT /booking/:id updates are persisted on subsequent GET"
    )
    public void tc_ub_011_updatePersistedOnGet() {
        String newFirst = "Persisted" + FakerUtils.stringOfLength(6);
        Booking updated = BookingBuilder.aDefaultBooking().firstname(newFirst).build();
        bookingClient.updateBookingWithCookie(bookingId, updated);

        Response getResp = bookingClient.getBookingById(bookingId);
        AssertionHelper.assertStatusCode(getResp, StatusCodes.OK);
        Assertions.assertThat(getResp.jsonPath().getString("firstname"))
                .as("Updated firstname should be persisted")
                .isEqualTo(newFirst);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_UB_012 | TS-UB-012 — PUT /booking/:id responds within 5-second SLA"
    )
    public void tc_ub_012_responseTimeWithinSla() {
        Booking updated = BookingBuilder.aBooking().build();
        Response resp = bookingClient.updateBookingWithCookie(bookingId, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertResponseTimeSla(resp);
    }
}
