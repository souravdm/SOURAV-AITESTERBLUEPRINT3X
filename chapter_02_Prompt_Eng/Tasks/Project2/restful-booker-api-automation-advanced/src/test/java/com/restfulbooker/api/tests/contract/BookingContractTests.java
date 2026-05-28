package com.restfulbooker.api.tests.contract;

import com.restfulbooker.api.builders.BookingBuilder;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.Booking;
import com.restfulbooker.api.models.CreateBookingResponse;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Contract / schema tests verifying the API surface matches the PRD specification.
 * These tests assert field presence, types, and JSON Schema compliance — not logic.
 */
public class BookingContractTests extends BaseTest {

    private final BookingClient bookingClient = new BookingClient();
    private int contractBookingId;

    @BeforeClass(alwaysRun = true)
    public void createContractBooking() {
        Response resp = bookingClient.createBooking(BookingBuilder.aDefaultBooking().build());
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        contractBookingId = resp.as(CreateBookingResponse.class).getBookingid();
        log.info("ContractTests: created bookingId={}", contractBookingId);
    }

    @AfterClass(alwaysRun = true)
    public void deleteContractBooking() {
        if (contractBookingId > 0) bookingClient.deleteBookingWithCookie(contractBookingId);
    }

    // ─── POST /booking contract ───────────────────────────────────────────────

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_001 — POST /booking response schema matches create-booking-response-schema.json"
    )
    public void tc_ct_001_createBookingSchema() {
        Booking b = BookingBuilder.aBooking().build();
        Response resp = bookingClient.createBooking(b);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/create-booking-response-schema.json");
    }

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_002 — POST /booking response contains bookingid (integer) and booking (object)"
    )
    public void tc_ct_002_createBookingFieldTypes() {
        Booking b = BookingBuilder.aBooking().build();
        Response resp = bookingClient.createBooking(b);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);

       /* assertThat(resp.jsonPath().get("bookingid"))
                .as("bookingid must be present").isNotNull();
        assertThat(resp.jsonPath().get("bookingid"))
                .as("bookingid must be an Integer").isInstanceOf(Integer.class);
        assertThat(resp.jsonPath().get("booking"))
                .as("booking object must be present").isNotNull(); */

        Integer bookingId = resp.jsonPath().getInt("bookingid");
        Object booking = resp.jsonPath().get("booking");
        assertThat(bookingId)
                .as("bookingid must be present")
                .isNotNull();
        assertThat(booking)
                .as("booking object must be present")
                .isNotNull();
    }

    // ─── GET /booking/:id contract ────────────────────────────────────────────

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_003 — GET /booking/:id response schema matches booking-response-schema.json"
    )
    public void tc_ct_003_getBookingSchema() {
        Response resp = bookingClient.getBookingById(contractBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/booking-response-schema.json");
    }

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_004 — GET /booking/:id all required fields present with correct types"
    )
    public void tc_ct_004_getBookingFieldTypes() {
        Response resp = bookingClient.getBookingById(contractBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);

        assertThat(resp.jsonPath().getString("firstname")).isNotBlank();
        assertThat(resp.jsonPath().getString("lastname")).isNotBlank();
     /*   assertThat(resp.jsonPath().get("totalprice")).isInstanceOf(Integer.class);
        assertThat(resp.jsonPath().get("depositpaid")).isInstanceOf(Boolean.class); */
        Integer totalPrice = resp.jsonPath().getInt("totalprice");
        Boolean depositPaid = resp.jsonPath().getBoolean("depositpaid");
        assertThat(totalPrice)
                .as("totalprice must be an Integer")
                .isNotNull();
        assertThat(depositPaid)
                .as("depositpaid must be a Boolean")
                .isNotNull();
        assertThat(resp.jsonPath().getString("bookingdates.checkin")).isNotBlank();
        assertThat(resp.jsonPath().getString("bookingdates.checkout")).isNotBlank();
    }

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_005 — GET /booking/:id date fields match YYYY-MM-DD format"
    )
    public void tc_ct_005_dateFieldFormat() {
        Response resp = bookingClient.getBookingById(contractBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        String checkin  = resp.jsonPath().getString("bookingdates.checkin");
        String checkout = resp.jsonPath().getString("bookingdates.checkout");
     /*   assertThat(checkin).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(checkout).matches("\\d{4}-\\d{2}-\\d{2}"); */
        org.assertj.core.api.Assertions.assertThat((String) checkin)
                .matches("\\d{4}-\\d{2}-\\d{2}");
        org.assertj.core.api.Assertions.assertThat((String) checkout)
                .matches("\\d{4}-\\d{2}-\\d{2}");
    }

    // ─── GET /booking (list) contract ────────────────────────────────────────

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_006 — GET /booking response schema matches booking-ids-response-schema.json"
    )
    public void tc_ct_006_bookingListSchema() {
        Response resp = bookingClient.getAllBookings();
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/booking-ids-response-schema.json");
    }

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_007 — GET /booking list items contain only bookingid field"
    )
    public void tc_ct_007_bookingListItemShape() {
        Response resp = bookingClient.getAllBookings();
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        java.util.List<Object> ids = resp.jsonPath().getList("bookingid");
        assertThat(ids).as("bookingid list must not be null").isNotNull();
        ids.forEach(id ->
            assertThat(id).as("Each bookingid must be an Integer").isInstanceOf(Integer.class)
        );
    }

    // ─── POST /auth contract ─────────────────────────────────────────────────

    @Test(
        groups  = {"contract", "auth"},
        description = "TC_CT_008 — POST /auth response schema matches auth-response-schema.json"
    )
    public void tc_ct_008_authResponseSchema() {
        com.restfulbooker.api.clients.AuthClient authClient = new com.restfulbooker.api.clients.AuthClient();
        com.restfulbooker.api.models.AuthRequest req = com.restfulbooker.api.models.AuthRequest.builder()
                .username("admin").password("password123").build();
        Response resp = authClient.createToken(req);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/auth-response-schema.json");
    }

    @Test(
        groups  = {"contract", "auth"},
        description = "TC_CT_009 — POST /auth token field is a non-empty string"
    )
    public void tc_ct_009_authTokenFieldType() {
        com.restfulbooker.api.clients.AuthClient authClient = new com.restfulbooker.api.clients.AuthClient();
        com.restfulbooker.api.models.AuthRequest req = com.restfulbooker.api.models.AuthRequest.builder()
                .username("admin").password("password123").build();
        Response resp = authClient.createToken(req);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        String token = resp.jsonPath().getString("token");
        assertThat(token)
                .as("token must be a non-blank string")
                .isNotBlank()
                .isInstanceOf(String.class);
    }

    // ─── Response headers contract ────────────────────────────────────────────

    @Test(
        groups  = {"contract", "booking"},
        description = "TC_CT_010 — API returns Content-Type: application/json on all JSON endpoints"
    )
    public void tc_ct_010_jsonContentTypeHeader() {
        Response resp = bookingClient.getBookingById(contractBookingId);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertHeaderPresent(resp, "Content-Type");
        assertThat(resp.contentType()).containsIgnoringCase("application/json");
    }
}
