package com.restfulbooker.api.tests.regression;

import com.restfulbooker.api.builders.BookingBuilder;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.CreateBookingResponse;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 * Regression tests for DELETE /booking/:id.
 * Test Plan scenarios: TS-DB-001 through TS-DB-008
 */
public class DeleteBookingTests extends BaseTest {

    private final BookingClient bookingClient = new BookingClient();

    private int createFreshBooking() {
        Response resp = bookingClient.createBooking(BookingBuilder.aBooking().build());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        return resp.as(CreateBookingResponse.class).getBookingid();
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_DB_001 | TS-DB-001 — DELETE /booking/:id with cookie auth returns 201 Created"
    )
    public void tc_db_001_deleteWithCookieReturns201() {
        int id = createFreshBooking();
        Response resp = bookingClient.deleteBookingWithCookie(id);
        AssertionHelper.assertStatusCode(resp, StatusCodes.CREATED);
        AssertionHelper.assertResponseTimeSla(resp);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_DB_002 | TS-DB-002 — DELETE /booking/:id with Basic Auth returns 201"
    )
    public void tc_db_002_deleteWithBasicAuthReturns201() {
        int id = createFreshBooking();
        Response resp = bookingClient.deleteBookingWithBasicAuth(id);
        AssertionHelper.assertStatusCode(resp, StatusCodes.CREATED);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_DB_003 | TS-DB-003 — Deleted booking returns 404 on subsequent GET"
    )
    public void tc_db_003_deletedBookingNotFoundOnGet() {
        int id = createFreshBooking();
        bookingClient.deleteBookingWithCookie(id);
        Response getResp = bookingClient.getBookingById(id);
        AssertionHelper.assertStatusCode(getResp, StatusCodes.NOT_FOUND);
    }

    @Test(
        groups  = {"regression", "booking", "auth"},
        description = "TC_DB_004 | TS-DB-004 — DELETE /booking/:id without auth returns 403"
    )
    public void tc_db_004_noAuthReturns403() {
        int id = createFreshBooking();
        try {
            Response resp = bookingClient.deleteBookingNoAuth(id);
            AssertionHelper.assertStatusCode(resp, StatusCodes.FORBIDDEN);
        } finally {
            bookingClient.deleteBookingWithCookie(id);
        }
    }

    @Test(
        groups  = {"regression", "booking", "auth"},
        description = "TC_DB_005 | TS-DB-005 — DELETE /booking/:id with invalid token returns 403"
    )
    public void tc_db_005_invalidTokenReturns403() {
        int id = createFreshBooking();
        try {
            Response resp = bookingClient.deleteBookingInvalidToken(id);
            AssertionHelper.assertStatusCode(resp, StatusCodes.FORBIDDEN);
        } finally {
            bookingClient.deleteBookingWithCookie(id);
        }
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_DB_006 | TS-DB-006 — DELETE /booking/:id on non-existent id returns 405 or 404"
    )
    public void tc_db_006_nonExistentIdReturns405() {
        Response resp = bookingClient.deleteBookingWithCookie(Integer.MAX_VALUE);
        Assertions.assertThat(resp.statusCode())
                .as("DELETE non-existent id: expected 404 or 405")
                .isIn(StatusCodes.NOT_FOUND, 405);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_DB_007 | TS-DB-007 — DELETE /booking/:id twice returns 405 on second attempt"
    )
    public void tc_db_007_deleteTwiceReturns405() {
        int id = createFreshBooking();
        bookingClient.deleteBookingWithCookie(id);
        Response secondDelete = bookingClient.deleteBookingWithCookie(id);
        Assertions.assertThat(secondDelete.statusCode())
                .as("Second DELETE should return 404 or 405")
                .isIn(StatusCodes.NOT_FOUND, 405);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_DB_008 | TS-DB-008 — DELETE /booking/:id responds within 5-second SLA"
    )
    public void tc_db_008_responseTimeWithinSla() {
        int id = createFreshBooking();
        Response resp = bookingClient.deleteBookingWithCookie(id);
        AssertionHelper.assertStatusCode(resp, StatusCodes.CREATED);
        AssertionHelper.assertResponseTimeSla(resp);
    }
}
