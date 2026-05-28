package com.restfulbooker.api.tests.e2e;

import com.restfulbooker.api.builders.BookingBuilder;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.Booking;
import com.restfulbooker.api.models.CreateBookingResponse;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.tests.base.ScenarioContext;
import com.restfulbooker.api.utils.AssertionHelper;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * End-to-end booking lifecycle flows.
 * Test Plan scenarios: TS-XC-001 through TS-XC-005
 *
 * Tests are ordered and share state via ScenarioContext. Run as a single-threaded
 * group to preserve order guarantees (configured in testsuites/e2e.xml).
 */
public class E2EBookingFlowTests extends BaseTest {

    private static final String KEY_BOOKING_ID = "e2e.booking.id";
    private static final String KEY_BOOKING    = "e2e.booking.original";

    private final BookingClient bookingClient = new BookingClient();

    @AfterClass(alwaysRun = true)
    public void cleanupIfNeeded() {
        Integer id = ScenarioContext.get(KEY_BOOKING_ID);
        if (id != null && id > 0) {
            bookingClient.deleteBookingWithCookie(id);
            log.info("E2E cleanup: deleted bookingId={}", id);
        }
        ScenarioContext.clear();
    }

    @Test(
        groups    = {"e2e", "booking"},
        priority  = 1,
        description = "TC_XC_001 | TS-XC-001 — E2E Step 1: Create a new booking and store bookingid"
    )
    public void tc_xc_001_createBooking() {
        Booking booking = BookingBuilder.aDefaultBooking()
                .firstname("E2EFirst").lastname("E2ELast").build();
        Response resp = bookingClient.createBooking(booking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);

        CreateBookingResponse created = resp.as(CreateBookingResponse.class);
        Assertions.assertThat(created.getBookingid()).isGreaterThan(0);
        Assertions.assertThat(created.getBooking().getFirstname()).isEqualTo("E2EFirst");

        ScenarioContext.set(KEY_BOOKING_ID, created.getBookingid());
        ScenarioContext.set(KEY_BOOKING, booking);
        log.info("E2E TC_XC_001: bookingId={}", created.getBookingid());
    }

    @Test(
        groups    = {"e2e", "booking"},
        priority  = 2,
        dependsOnMethods = "tc_xc_001_createBooking",
        description = "TC_XC_002 | TS-XC-002 — E2E Step 2: GET /booking/:id returns the created booking"
    )
    public void tc_xc_002_readBooking() {
        int id = ScenarioContext.get(KEY_BOOKING_ID);
        Response resp = bookingClient.getBookingById(id);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);

        Assertions.assertThat(resp.jsonPath().getString("firstname")).isEqualTo("E2EFirst");
        Assertions.assertThat(resp.jsonPath().getString("lastname")).isEqualTo("E2ELast");
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/booking-response-schema.json");
        log.info("E2E TC_XC_002: verified bookingId={}", id);
    }

    @Test(
        groups    = {"e2e", "booking"},
        priority  = 3,
        dependsOnMethods = "tc_xc_002_readBooking",
        description = "TC_XC_003 | TS-XC-003 — E2E Step 3: Full update via PUT /booking/:id"
    )
    public void tc_xc_003_updateBooking() {
        int id = ScenarioContext.get(KEY_BOOKING_ID);
        Booking updated = BookingBuilder.aDefaultBooking()
                .firstname("E2EUpdated").lastname("E2ELast").totalprice(500).build();
        Response resp = bookingClient.updateBookingWithCookie(id, updated);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);

        Assertions.assertThat(resp.jsonPath().getString("firstname")).isEqualTo("E2EUpdated");
        Assertions.assertThat(resp.jsonPath().getInt("totalprice")).isEqualTo(500);
        log.info("E2E TC_XC_003: updated bookingId={}", id);
    }

    @Test(
        groups    = {"e2e", "booking"},
        priority  = 4,
        dependsOnMethods = "tc_xc_003_updateBooking",
        description = "TC_XC_004 | TS-XC-004 — E2E Step 4: Partial update via PATCH /booking/:id"
    )
    public void tc_xc_004_patchBooking() {
        int id = ScenarioContext.get(KEY_BOOKING_ID);
        Response resp = bookingClient.patchBookingWithCookie(id,
                java.util.Map.of("additionalneeds", "Extra towels", "totalprice", 600));
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);

        Assertions.assertThat(resp.jsonPath().getString("additionalneeds")).isEqualTo("Extra towels");
        Assertions.assertThat(resp.jsonPath().getInt("totalprice")).isEqualTo(600);
        Assertions.assertThat(resp.jsonPath().getString("firstname"))
                .as("Unpatched firstname must be preserved")
                .isEqualTo("E2EUpdated");
        log.info("E2E TC_XC_004: patched bookingId={}", id);
    }

    @Test(
        groups    = {"e2e", "booking"},
        priority  = 5,
        dependsOnMethods = "tc_xc_004_patchBooking",
        description = "TC_XC_005 | TS-XC-005 — E2E Step 5: DELETE /booking/:id and verify 404 on GET"
    )
    public void tc_xc_005_deleteBooking() {
        int id = ScenarioContext.get(KEY_BOOKING_ID);
        Response delResp = bookingClient.deleteBookingWithCookie(id);
        AssertionHelper.assertStatusCode(delResp, StatusCodes.CREATED);

        Response getResp = bookingClient.getBookingById(id);
        AssertionHelper.assertStatusCode(getResp, StatusCodes.NOT_FOUND);

        ScenarioContext.set(KEY_BOOKING_ID, 0);
        log.info("E2E TC_XC_005: deleted and verified bookingId={} returns 404", id);
    }
}
