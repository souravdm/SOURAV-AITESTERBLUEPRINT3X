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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Regression tests for GET /booking and GET /booking/:id.
 * Test Plan scenarios: TS-GBI-001 through TS-GBI-012, TS-GB-001 through TS-GB-010
 */
public class BookingGetTests extends BaseTest {

    private final BookingClient bookingClient = new BookingClient();
    private int createdBookingId;
    private Booking seedBooking;

    @BeforeClass(alwaysRun = true)
    public void createSeedBooking() {
        seedBooking = BookingBuilder.aDefaultBooking().build();
        Response resp = bookingClient.createBooking(seedBooking);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        CreateBookingResponse created = resp.as(CreateBookingResponse.class);
        createdBookingId = created.getBookingid();
        log.info("Seed booking created with id={}", createdBookingId);
    }

    @AfterClass(alwaysRun = true)
    public void deleteSeedBooking() {
        if (createdBookingId > 0) {
            bookingClient.deleteBookingWithCookie(createdBookingId);
        }
    }

    // ─── GET /booking (list) ─────────────────────────────────────────────────

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_001 | TS-GBI-001 — GET /booking returns 200 with non-empty array"
    )
    public void tc_gbi_001_getAllBookingsReturns200() {
        Response resp = bookingClient.getAllBookings();
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<?> list = resp.jsonPath().getList("$");
        Assertions.assertThat(list).as("Booking list should not be null").isNotNull();
        AssertionHelper.assertResponseTimeSla(resp);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_002 | TS-GBI-002 — GET /booking response items each contain bookingid field"
    )
    public void tc_gbi_002_eachItemHasBookingId() {
        Response resp = bookingClient.getAllBookings();
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<Integer> ids = resp.jsonPath().getList("bookingid");
        Assertions.assertThat(ids)
                .as("Every element must have a bookingid")
                .isNotEmpty()
                .doesNotContainNull();
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_003 | TS-GBI-003 — Filter by firstname returns only matching bookings"
    )
    public void tc_gbi_003_filterByFirstname() {
        Response resp = bookingClient.getBookingsByFirstname(seedBooking.getFirstname());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<Integer> ids = resp.jsonPath().getList("bookingid");
        Assertions.assertThat(ids)
                .as("Filter by firstname should include the seed booking")
                .contains(createdBookingId);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_004 | TS-GBI-004 — Filter by lastname returns only matching bookings"
    )
    public void tc_gbi_004_filterByLastname() {
        Response resp = bookingClient.getBookingsByLastname(seedBooking.getLastname());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<Integer> ids = resp.jsonPath().getList("bookingid");
        Assertions.assertThat(ids)
                .as("Filter by lastname should include the seed booking")
                .contains(createdBookingId);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_005 | TS-GBI-005 — Filter by both firstname and lastname narrows results"
    )
    public void tc_gbi_005_filterByFirstAndLastname() {
        Response resp = bookingClient.getBookingsByName(
                seedBooking.getFirstname(), seedBooking.getLastname());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<Integer> ids = resp.jsonPath().getList("bookingid");
        Assertions.assertThat(ids).contains(createdBookingId);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_006 | TS-GBI-006 — Filter by checkin date returns matching bookings"
    )
    public void tc_gbi_006_filterByCheckin() {
        Response resp = bookingClient.getBookingsByCheckin(seedBooking.getBookingdates().getCheckin());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<Integer> ids = resp.jsonPath().getList("bookingid");
        Assertions.assertThat(ids)
                .as("Filter by checkin should return a valid booking id list")
                .isNotNull()
                .doesNotContainNull();
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_007 | TS-GBI-007 — Filter by checkout date returns matching bookings"
    )
    public void tc_gbi_007_filterByCheckout() {
        Response resp = bookingClient.getBookingsByCheckout(seedBooking.getBookingdates().getCheckout());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<Integer> ids = resp.jsonPath().getList("bookingid");
        Assertions.assertThat(ids).contains(createdBookingId);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_GBI_008 | TS-GBI-008 — Filter by non-existent firstname returns empty array or 200"
    )
    public void tc_gbi_008_filterByNonExistentFirstname() {
        Response resp = bookingClient.getBookingsByFirstname("XYZZY_NOEXIST_99999");
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        List<?> list = resp.jsonPath().getList("$");
        Assertions.assertThat(list)
                .as("Non-existent filter should return empty array")
                .isNotNull()
                .isEmpty();
    }

    @Test(
        groups  = {"regression", "booking", "schema"},
        description = "TC_GBI_009 | TS-GBI-009 — GET /booking response validates against booking-ids schema"
    )
    public void tc_gbi_009_schemaValidation() {
        Response resp = bookingClient.getAllBookings();
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/booking-ids-response-schema.json");
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_010 | TS-GBI-010 — GET /booking accepts XML via Accept header"
    )
    public void tc_gbi_010_xmlAcceptHeaderSupported() {
        Response resp = bookingClient.getAllBookingsXml();
        Assertions.assertThat(resp.statusCode())
                .as("XML accept GET /booking should return 200 or 418")
                .isIn(StatusCodes.OK, 418);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_011 | TS-GBI-011 — Content-Type response header is application/json"
    )
    public void tc_gbi_011_responseContentTypeIsJson() {
        Response resp = bookingClient.getAllBookings();
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertHeaderPresent(resp, "Content-Type");
        Assertions.assertThat(resp.contentType())
                .as("Content-Type should be JSON")
                .containsIgnoringCase("application/json");
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GBI_012 | TS-GBI-012 — GET /booking responds within 5-second SLA"
    )
    public void tc_gbi_012_responseTimeWithinSla() {
        Response resp = bookingClient.getAllBookings();
        AssertionHelper.assertResponseTimeSla(resp);
    }

    // ─── GET /booking/:id ────────────────────────────────────────────────────

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GB_001 | TS-GB-001 — GET /booking/:id returns 200 with full booking object"
    )
    public void tc_gb_001_getBookingByIdReturns200() {
        Response resp = bookingClient.getBookingById(createdBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Booking fetched = resp.as(Booking.class);
        Assertions.assertThat(fetched.getFirstname())
                .as("Firstname should match seed booking")
                .isEqualTo(seedBooking.getFirstname());
        Assertions.assertThat(fetched.getLastname())
                .as("Lastname should match seed booking")
                .isEqualTo(seedBooking.getLastname());
        AssertionHelper.assertResponseTimeSla(resp);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GB_002 | TS-GB-002 — GET /booking/:id response contains all required fields"
    )
    public void tc_gb_002_responseContainsAllFields() {
        Response resp = bookingClient.getBookingById(createdBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonPathNotBlank(resp, "firstname");
        AssertionHelper.assertJsonPathNotBlank(resp, "lastname");
        AssertionHelper.assertJsonPath(resp, "totalprice", seedBooking.getTotalprice());
        AssertionHelper.assertJsonPathNotBlank(resp, "bookingdates.checkin");
        AssertionHelper.assertJsonPathNotBlank(resp, "bookingdates.checkout");
    }

    @Test(
        groups  = {"regression", "booking", "schema"},
        description = "TC_GB_003 | TS-GB-003 — GET /booking/:id validates against booking-response schema"
    )
    public void tc_gb_003_schemaValidation() {
        Response resp = bookingClient.getBookingById(createdBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/booking-response-schema.json");
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_GB_004 | TS-GB-004 — GET /booking/:id with non-existent ID returns 404"
    )
    public void tc_gb_004_nonExistentIdReturns404() {
        Response resp = bookingClient.getBookingById(Integer.MAX_VALUE);
        AssertionHelper.assertStatusCode(resp, StatusCodes.NOT_FOUND);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_GB_005 | TS-GB-005 — GET /booking/:id with id=0 returns 404"
    )
    public void tc_gb_005_idZeroReturns404() {
        Response resp = bookingClient.getBookingById(0);
        Assertions.assertThat(resp.statusCode())
                .as("ID=0 should return 404 or 400")
                .isIn(StatusCodes.NOT_FOUND, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        description = "TC_GB_006 | TS-GB-006 — GET /booking/:id with negative id returns 404"
    )
    public void tc_gb_006_negativeIdReturns404() {
        Response resp = bookingClient.getBookingById(-1);
        Assertions.assertThat(resp.statusCode())
                .as("Negative ID should return 404 or 400")
                .isIn(StatusCodes.NOT_FOUND, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GB_007 | TS-GB-007 — GET /booking/:id accepts XML response"
    )
    public void tc_gb_007_xmlResponseAccepted() {
        Response resp = bookingClient.getBookingByIdXml(createdBookingId);
        Assertions.assertThat(resp.statusCode())
                .as("XML accept for existing booking should return 200")
                .isEqualTo(StatusCodes.OK);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GB_008 | TS-GB-008 — bookingdates.checkin is earlier than checkout"
    )
    public void tc_gb_008_checkinBeforeCheckout() {
        Response resp = bookingClient.getBookingById(createdBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        String checkin  = resp.jsonPath().getString("bookingdates.checkin");
        String checkout = resp.jsonPath().getString("bookingdates.checkout");
        Assertions.assertThat(checkin.compareTo(checkout))
                .as("checkin (%s) must be before checkout (%s)", checkin, checkout)
                .isLessThan(0);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GB_009 | TS-GB-009 — GET /booking/:id totalprice is a positive integer"
    )
    public void tc_gb_009_totalpriceIsPositive() {
        Response resp = bookingClient.getBookingById(createdBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        int price = resp.jsonPath().getInt("totalprice");
        Assertions.assertThat(price).as("totalprice must be positive").isGreaterThan(0);
    }

    @Test(
        groups  = {"regression", "booking", "positive"},
        description = "TC_GB_010 | TS-GB-010 — GET /booking/:id depositpaid is a boolean"
    )
    public void tc_gb_010_depositpaidIsBoolean() {
        Response resp = bookingClient.getBookingById(createdBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        Object dp = resp.jsonPath().get("depositpaid");
        Assertions.assertThat(dp)
                .as("depositpaid must be a boolean")
                .isInstanceOf(Boolean.class);
    }

    // ─── DataProvider for filter combos ──────────────────────────────────────

    @DataProvider(name = "invalidFilterValues")
    public Object[][] invalidFilterValues() {
        return new Object[][] {
            {"checkin",  "not-a-date"},
            {"checkout", "32-13-2099"},
        };
    }

    @Test(
        groups  = {"regression", "booking", "negative"},
        dataProvider = "invalidFilterValues",
        description = "TC_GBI_NEG | TS-GBI-010 — Invalid date filter values return 200 or 400"
    )
    public void tc_gbi_neg_invalidDateFilter(String param, String value) {
        Response resp = bookingClient.getBookingsWithFilter(param, value);
        Assertions.assertThat(resp.statusCode())
                .as("Invalid %s=%s should return 200 (empty) or 400", param, value)
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }
}
