package com.restfulbooker.api.tests.regression;

import com.restfulbooker.api.clients.AuthClient;
import com.restfulbooker.api.config.ConfigManager;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.models.AuthRequest;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Functional tests for POST /auth.
 * Test Plan scenarios: TS-AUTH-001 through TS-AUTH-010
 */
public class AuthTests extends BaseTest {

    private final AuthClient authClient = new AuthClient();

    @Test(
        groups  = {"regression", "auth", "positive"},
        description = "TC_AUTH_001 | TS-AUTH-001 — Valid credentials return 200 with non-empty token"
    )
    public void tc_auth_001_validCredentialsReturnToken() {
        AuthRequest req = AuthRequest.builder()
                .username(ConfigManager.getInstance().getRequired("auth.username"))
                .password(ConfigManager.getInstance().getRequired("auth.password"))
                .build();
        Response resp = authClient.createToken(req);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonPathNotBlank(resp, "token");
        AssertionHelper.assertResponseTimeSla(resp);
    }

    @Test(
        groups  = {"regression", "auth", "negative"},
        description = "TC_AUTH_002 | TS-AUTH-002 — Invalid password returns failure response"
    )
    public void tc_auth_002_invalidPasswordReturnsFailure() {
        AuthRequest req = AuthRequest.builder().username("admin").password("wrongpass").build();
        Response resp = authClient.createToken(req);
        // The API returns 200 with {"reason":"Bad credentials"} for invalid creds
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        String body = resp.body().asString();
        org.assertj.core.api.Assertions.assertThat(body)
                .as("Expected failure body for invalid password").doesNotContain("\"token\"");
    }

    @Test(
        groups  = {"regression", "auth", "negative"},
        description = "TC_AUTH_003 | TS-AUTH-003 — Invalid username returns failure response"
    )
    public void tc_auth_003_invalidUsernameReturnsFailure() {
        AuthRequest req = AuthRequest.builder().username("nobody").password("password123").build();
        Response resp = authClient.createToken(req);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        String body = resp.body().asString();
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("\"token\"");
    }

    @Test(
        groups  = {"regression", "auth", "negative"},
        description = "TC_AUTH_004 | TS-AUTH-004 — Missing username field"
    )
    public void tc_auth_004_missingUsername() {
        Response resp = authClient.createTokenRaw(Map.of("password", "password123"));
        // The API is lenient — capture actual behavior; must not return a valid token
        String body = resp.body().asString();
        org.assertj.core.api.Assertions.assertThat(body)
                .as("Response with missing username should not contain a token")
                .doesNotContain("\"token\"");
    }

    @Test(
        groups  = {"regression", "auth", "negative"},
        description = "TC_AUTH_005 | TS-AUTH-005 — Missing password field"
    )
    public void tc_auth_005_missingPassword() {
        Response resp = authClient.createTokenRaw(Map.of("username", "admin"));
        String body = resp.body().asString();
        org.assertj.core.api.Assertions.assertThat(body)
                .as("Response with missing password should not contain a token")
                .doesNotContain("\"token\"");
    }

    @Test(
        groups  = {"regression", "auth", "negative"},
        description = "TC_AUTH_006 | TS-AUTH-006 — Empty JSON body"
    )
    public void tc_auth_006_emptyBody() {
        Response resp = authClient.createTokenRaw(Map.of());
        String body = resp.body().asString();
        org.assertj.core.api.Assertions.assertThat(body).doesNotContain("\"token\"");
    }

    @Test(
        groups  = {"regression", "auth", "schema"},
        description = "TC_AUTH_009 | TS-AUTH-009 — Response schema contains only the 'token' field"
    )
    public void tc_auth_009_responseSchemaValidation() {
        AuthRequest req = AuthRequest.builder().username("admin").password("password123").build();
        Response resp = authClient.createToken(req);
        AssertionHelper.assertStatusCode(resp, StatusCodes.OK);
        AssertionHelper.assertJsonSchema(resp, "testdata/schemas/auth-response-schema.json");
    }

    @Test(
        groups  = {"regression", "auth", "security"},
        description = "TC_AUTH_008 | TS-AUTH-008 — SQL injection in username does not return token"
    )
    public void tc_auth_008_sqlInjectionInUsername() {
        AuthRequest req = AuthRequest.builder()
                .username("admin' OR '1'='1").password("password123").build();
        Response resp = authClient.createToken(req);
        String body = resp.body().asString();
        org.assertj.core.api.Assertions.assertThat(body)
                .as("SQL injection should not yield a valid token").doesNotContain("\"token\"");
    }
}
