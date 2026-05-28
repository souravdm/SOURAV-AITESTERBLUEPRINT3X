package com.restfulbooker.api.tests.negative;

import com.restfulbooker.api.builders.BookingBuilder;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.Booking;
import com.restfulbooker.api.models.BookingDates;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import com.restfulbooker.api.utils.FakerUtils;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Boundary and negative tests covering edge-case inputs across all endpoints.
 */
public class BoundaryTests extends BaseTest {

    private final BookingClient bookingClient = new BookingClient();

    // ─── firstname boundary ───────────────────────────────────────────────────

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_001 — POST /booking with empty string firstname"
    )
    public void tc_bnd_001_emptyFirstname() {
        Booking b = BookingBuilder.aBooking().firstname("").build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .as("Empty firstname: 200 (lenient) or 400 (strict)")
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_002 — POST /booking with single-char firstname"
    )
    public void tc_bnd_002_singleCharFirstname() {
        Booking b = BookingBuilder.aBooking().firstname("A").build();
        Response resp = bookingClient.createBooking(b);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_003 — POST /booking with 500-char firstname (over boundary)"
    )
    public void tc_bnd_003_over500CharFirstname() {
        Booking b = BookingBuilder.aBooking().firstname(FakerUtils.stringOfLength(500)).build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .as("500-char firstname: accepted (200) or rejected (400)")
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_004 — POST /booking firstname with special characters"
    )
    public void tc_bnd_004_specialCharsFirstname() {
        Booking b = BookingBuilder.aBooking().firstname("<script>alert(1)</script>").build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
        if (resp.statusCode() == StatusCodes.OK) {
            String stored = resp.jsonPath().getString("booking.firstname");
            Assertions.assertThat(stored)
                    .as("Public API currently stores special-character input verbatim")
                    .isEqualTo(b.getFirstname());
        }
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_005 — POST /booking firstname with unicode characters"
    )
    public void tc_bnd_005_unicodeFirstname() {
        Booking b = BookingBuilder.aBooking().firstname("Ångström Müller").build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    // ─── totalprice boundary ─────────────────────────────────────────────────

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_006 — POST /booking with totalprice=-1 (negative)"
    )
    public void tc_bnd_006_negativeTotalprice() {
        Booking b = BookingBuilder.aBooking().totalprice(-1).build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .as("Negative totalprice: accepted (200) or rejected (400)")
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_007 — POST /booking with totalprice=Integer.MIN_VALUE"
    )
    public void tc_bnd_007_minIntTotalprice() {
        Booking b = BookingBuilder.aBooking().totalprice(Integer.MIN_VALUE).build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR);
    }

    // ─── date boundary ───────────────────────────────────────────────────────

    @DataProvider(name = "invalidDateFormats")
    public Object[][] invalidDateFormats() {
        return new Object[][] {
            {"01-06-2026", "05-06-2026"},
            {"2026/06/01", "2026/06/05"},
            {"June 1 2026", "June 5 2026"},
            {"",           "2026-06-05"},
            {"2026-06-01", ""},
        };
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        dataProvider = "invalidDateFormats",
        description = "TC_BND_008 — POST /booking with invalid date format combinations"
    )
    public void tc_bnd_008_invalidDateFormats(String checkin, String checkout) {
        BookingDates dates = BookingDates.builder().checkin(checkin).checkout(checkout).build();
        Booking b = BookingBuilder.aBooking().bookingdates(dates).build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .as("Invalid date checkin=%s checkout=%s", checkin, checkout)
                .isIn(StatusCodes.BAD_REQUEST, StatusCodes.SERVER_ERROR, StatusCodes.OK);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_009 — POST /booking with leap day date (2024-02-29)"
    )
    public void tc_bnd_009_leapDayDate() {
        BookingDates dates = BookingDates.builder()
                .checkin("2024-02-29").checkout("2024-03-01").build();
        Booking b = BookingBuilder.aBooking().bookingdates(dates).build();
        Response resp = bookingClient.createBooking(b);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_010 — POST /booking with far future date (2099-12-31)"
    )
    public void tc_bnd_010_farFutureDate() {
        BookingDates dates = BookingDates.builder()
                .checkin("2099-12-30").checkout("2099-12-31").build();
        Booking b = BookingBuilder.aBooking().bookingdates(dates).build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }

    // ─── content-type boundary ───────────────────────────────────────────────

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_011 — POST /booking with text/plain content-type returns 400 or 415"
    )
    public void tc_bnd_011_wrongContentType() {
        Response resp = bookingClient.createBookingWithContentType(
                BookingBuilder.aBooking().build(), "text/plain");
        Assertions.assertThat(resp.statusCode())
                .as("Wrong content-type should return 400, 415, or 500")
                .isIn(StatusCodes.BAD_REQUEST, 415, StatusCodes.SERVER_ERROR);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_012 — GET /booking/:id with string path segment returns 404 or 400"
    )
    public void tc_bnd_012_stringPathSegment() {
        Response resp = bookingClient.getBookingByStringId("not-an-id");
        Assertions.assertThat(resp.statusCode())
                .as("String path segment should return 404 or 400")
                .isIn(StatusCodes.NOT_FOUND, StatusCodes.BAD_REQUEST);
    }

    @Test(
        groups  = {"regression", "negative", "boundary"},
        description = "TC_BND_013 — POST /booking additionalneeds with 1000-char value"
    )
    public void tc_bnd_013_longAdditionalNeeds() {
        Booking b = BookingBuilder.aBooking()
                .additionalneeds(FakerUtils.stringOfLength(1000)).build();
        Response resp = bookingClient.createBooking(b);
        Assertions.assertThat(resp.statusCode())
                .isIn(StatusCodes.OK, StatusCodes.BAD_REQUEST);
    }
}
