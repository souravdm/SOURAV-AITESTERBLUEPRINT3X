package com.restfulbooker.api.tests.smoke;

import com.restfulbooker.api.clients.AuthClient;
import com.restfulbooker.api.clients.BookingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.AuthRequest;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import io.restassured.response.Response;
import org.testng.annotations.Test;

/**
 * Smoke tests: POST /auth and GET /booking.
 * Test Plan scenarios: TS-AUTH-001, TS-GBI-001
 */
public class AuthSmokeTests extends BaseTest {

    private final AuthClient    authClient    = new AuthClient();
    private final BookingClient bookingClient = new BookingClient();

    @Test(
        groups  = {"smoke", "auth"},
        description = "TC_AUTH_001 | TS-AUTH-001 — Valid credentials return HTTP 200 with non-empty token"
    )
    public void tc_auth_001_validCredentialsReturnToken() {
        AuthRequest req = AuthRequest.builder()
                .username("admin").password("password123").build();
        Response response = authClient.createToken(req);

        AssertionHelper.assertStatusCode(response, StatusCodes.OK);
        AssertionHelper.assertJsonPathNotBlank(response, "token");
        AssertionHelper.assertResponseTimeSla(response);
    }

    @Test(
        groups  = {"smoke", "booking"},
        description = "TC_GBI_001 | TS-GBI-001 — GET /booking returns HTTP 200 with array of bookingid objects"
    )
    public void tc_gbi_001_getAllBookingsReturnsArray() {
        Response response = bookingClient.getAllBookings();

        AssertionHelper.assertStatusCode(response, StatusCodes.OK);
        // Response must be a JSON array
        org.assertj.core.api.Assertions.assertThat(response.jsonPath().getList("$"))
                .as("GET /booking should return an array")
                .isNotNull();
        AssertionHelper.assertResponseTimeSla(response);
    }
}
