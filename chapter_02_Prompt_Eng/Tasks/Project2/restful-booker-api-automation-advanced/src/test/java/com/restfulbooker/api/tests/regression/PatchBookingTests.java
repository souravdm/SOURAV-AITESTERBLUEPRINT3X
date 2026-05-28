package com.restfulbooker.api.tests.regression;

import com.restfulbooker.api.builders.BookingBuilder;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.Booking;
import com.restfulbooker.api.models.CreateBookingResponse;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Regression tests for PATCH /booking/:id.
 * Test Plan scenarios: TS-PB-001 through TS-PB-011
 */
public class PatchBookingTests extends BaseTest {

    private final BookingClient bookingClient = new BookingClient();
    private int bookingId;
    private Booking originalBooking;

    @BeforeClass(alwaysRun = true)
    public void createBooking() {
        originalBooking = BookingBuilder.aDefaultBooking().build();
        Response resp = bookingClient.createBooking(originalBooking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        bookingId = resp.as(CreateBookingResponse.class).getBookingid();
        log.info("PatchBookingTests: created bookingId={}", bookingId);
    }

    @AfterClass(alwaysRun = true)
    public void deleteBooking() {
        if (bookingId > 0) bookingClient.deleteBookingWithCookie(bookingId);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_PB_001 | TS-PB-001 — PATCH /booking/:id updating only firstname with cookie auth returns 200"
    )
    public void tc_pb_001_patchFirstnameWithCookieReturns200() {
        Response resp = bookingClient.patchBookingWithCookie(bookingId, Map.of("firstname", "PatchedFirst"));
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Assertions.assertThat(resp.jsonPath().getString("firstname"))
                .as("firstname should be patched")
                .isEqualTo("PatchedFirst");
        AssertionHelper.assertResponseTimeSla(resp);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_PB_002 | TS-PB-002 — PATCH /booking/:id updating only lastname with Basic Auth returns 200"
    )
    public void tc_pb_002_patchLastnameWithBasicAuthReturns200() {
        Response resp = bookingClient.patchBookingWithBasicAuth(bookingId, Map.of("lastname", "PatchedLast"));
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Assertions.assertThat(resp.jsonPath().getString("lastname")).isEqualTo("PatchedLast");
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_PB_003 | TS-PB-003 — PATCH /booking/:id updating totalprice leaves other fields unchanged"
    )
    public void tc_pb_003_patchTotalpriceOtherFieldsUnchanged() {
        bookingClient.updateBookingWithCookie(bookingId, originalBooking);
        Response patchResp = bookingClient.patchBookingWithCookie(bookingId, Map.of("totalprice", 777));
        AssertionHelper.assertStatusCode(patchResp, StatusCodes.OK);
        Assertions.assertThat(patchResp.jsonPath().getInt("totalprice")).isEqualTo(777);
        Assertions.assertThat(patchResp.jsonPath().getString("firstname"))
                .as("Unpatched firstname should remain")
                .isEqualTo(originalBooking.getFirstname());
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_PB_004 | TS-PB-004 — PATCH /booking/:id updating depositpaid boolean"
    )
    public void tc_pb_004_patchDepositpaid() {
        boolean current = Boolean.TRUE.equals(originalBooking.getDepositpaid());
        Response resp = bookingClient.patchBookingWithCookie(bookingId, Map.of("depositpaid", !current));
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Assertions.assertThat(resp.jsonPath().getBoolean("depositpaid")).isEqualTo(!current);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_PB_005 | TS-PB-005 — PATCH /booking/:id updating bookingdates.checkin"
    )
    public void tc_pb_005_patchCheckinDate() {
        Map<String, Object> patch = Map.of("bookingdates", Map.of(
                "checkin", "2026-07-01",
                "checkout", originalBooking.getBookingdates().getCheckout()));
        Response resp = bookingClient.patchBookingWithCookie(bookingId, patch);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Assertions.assertThat(resp.jsonPath().getString("bookingdates.checkin"))
                .isEqualTo("2026-07-01");
    }

    @Test(
        groups  = {"regression", "booking", "schema"},
        description = "TC_PB_006 | TS-PB-006 — PATCH /booking/:id response validates against booking-response schema"
    )
    public void tc_pb_006_schemaValidation() {
        Response resp = bookingClient.patchBookingWithCookie(bookingId, Map.of("additionalneeds", "Late checkout"));
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/booking-response-schema.json");
    }

    @Test(
        groups  = {"regression", "booking", "auth"},
        description = "TC_PB_007 | TS-PB-007 — PATCH /booking/:id without auth returns 403"
    )
    public void tc_pb_007_noAuthReturns403() {
        Response resp = bookingClient.patchBookingNoAuth(bookingId, Map.of("firstname", "Hacker"));
        AssertionHelper.assertStatusCode(resp, StatusCodes.FORBIDDEN);
    }

    @Test(
        groups  = {"regression", "booking", "auth"},
        description = "TC_PB_008 | TS-PB-008 — PATCH /booking/:id with invalid token returns 403"
    )
    public void tc_pb_008_invalidTokenReturns403() {
        Response resp = bookingClient.patchBookingInvalidToken(bookingId, Map.of("firstname", "Hacker"));
        AssertionHelper.assertStatusCode(resp, StatusCodes.FORBIDDEN);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_PB_009 | TS-PB-009 — PATCH /booking/:id on non-existent id returns 405 or 404"
    )
    public void tc_pb_009_nonExistentIdReturns405Or404() {
        Response resp = bookingClient.patchBookingWithCookie(Integer.MAX_VALUE, Map.of("firstname", "Ghost"));
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.NOT_FOUND, 405);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_PB_010 | TS-PB-010 — PATCH /booking/:id patch is persisted on subsequent GET"
    )
    public void tc_pb_010_patchPersistedOnGet() {
        String newNeeds = "Extra pillow";
        bookingClient.patchBookingWithCookie(bookingId, Map.of("additionalneeds", newNeeds));
        Response getResp = bookingClient.getBookingById(bookingId);
        AssertionHelper.assertStatusCode(getResp, StatusCodes.OK);
        Assertions.assertThat(getResp.jsonPath().getString("additionalneeds"))
                .as("PATCH additionalneeds should be persisted")
                .isEqualTo(newNeeds);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_PB_011 | TS-PB-011 — PATCH /booking/:id responds within 5-second SLA"
    )
    public void tc_pb_011_responseTimeWithinSla() {
        Response resp = bookingClient.patchBookingWithCookie(bookingId, Map.of("lastname", "SlaTest"));
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertResponseTimeSla(resp);
    }
}
