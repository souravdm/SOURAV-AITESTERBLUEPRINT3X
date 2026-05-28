package com.restfulbooker.automation.tests;

import com.restfulbooker.automation.services.HealthCheckService;
import com.restfulbooker.automation.utils.JsonUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test class for the Restful Booker health-check endpoint.
 *
 * <p><b>Endpoint under test</b><br>
 * {@code GET /ping} — Source:
 * <a href="https://restful-booker.herokuapp.com/apidoc/index.html#api-Ping-Ping">
 *     Restful Booker API Docs — Ping</a>
 *
 * <p><b>Spec-derived facts (no inference)</b>
 * <ul>
 *   <li>HTTP method: {@code GET}</li>
 *   <li>Status code: {@code 201 Created}</li>
 *   <li>Response body: {@code Created} (plain text, case-sensitive)</li>
 *   <li>Content-Type: {@code text/plain; charset=utf-8}</li>
 * </ul>
 *
 * <p><b>Methods tagged [Inference]</b> exercise behaviours not explicitly
 * documented in the Restful Booker spec.  They are labelled
 * <em>Inference (low confidence)</em> inline and in the corresponding
 * {@link HealthCheckService} Javadoc.  Update expected status codes if live
 * behaviour differs.
 *
 * <p><b>Traceability matrix</b>
 * <table border="1">
 * <tr><th>Test ID</th><th>Req ID</th><th>Method</th><th>Scenario</th></tr>
 * <tr><td>TC-API-001</td><td>REQ-HC-001</td><td>GET</td>
 *     <td>201 status code — positive</td></tr>
 * <tr><td>TC-API-002</td><td>REQ-HC-002</td><td>GET</td>
 *     <td>Response body equals "Created" — positive</td></tr>
 * <tr><td>TC-API-003</td><td>REQ-HC-003</td><td>GET</td>
 *     <td>Content-Type contains "text/plain" — positive</td></tr>
 * <tr><td>TC-API-004</td><td>REQ-HC-004</td><td>GET</td>
 *     <td>Response time ≤ 2 000 ms SLA — performance</td></tr>
 * <tr><td>TC-API-005</td><td>REQ-HC-005</td><td>HEAD</td>
 *     <td>201 with empty body [Inference]</td></tr>
 * <tr><td>TC-API-006</td><td>REQ-HC-006</td><td>POST</td>
 *     <td>Invalid method → non-2xx [Inference]</td></tr>
 * <tr><td>TC-API-007</td><td>REQ-HC-007</td><td>PUT</td>
 *     <td>Invalid method → non-2xx [Inference]</td></tr>
 * <tr><td>TC-API-008</td><td>REQ-HC-008</td><td>DELETE</td>
 *     <td>Invalid method → non-2xx [Inference]</td></tr>
 * <tr><td>TC-API-009</td><td>REQ-HC-009</td><td>PATCH</td>
 *     <td>Invalid method → non-2xx [Inference]</td></tr>
 * <tr><td>TC-API-010</td><td>REQ-HC-010</td><td>GET ×3</td>
 *     <td>Idempotency — all hits return 201</td></tr>
 * <tr><td>TC-API-011</td><td>REQ-HC-011</td><td>GET</td>
 *     <td>Malformed Accept header ignored [Inference]</td></tr>
 * <tr><td>TC-API-012</td><td>REQ-HC-012</td><td>GET</td>
 *     <td>Unreachable host raises connectivity exception</td></tr>
 * </table>
 */
@Epic("Restful Booker API")
@Feature("Health Check")
public class PingTests extends BaseTest {

    /** Loaded once per class from src/test/resources/testdata/ping.json */
    private Map<String, Object> pingTestData;

    /** SLA ceiling derived from config — not hardcoded here. */
    private long slaMs;

    // ── Test data / pre-conditions ───────────────────────────────────────────

    @BeforeClass(alwaysRun = true)
    public void loadTestData() {
        pingTestData = JsonUtils.readMapFromClasspath("testdata/ping.json");
        slaMs = config.getResponseTimeSlaMs();
        log.info("Loaded ping test data: {}", pingTestData);
        log.info("Response-time SLA: {}ms", slaMs);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-001 ─ Positive: status code
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-001 | Endpoint: GET /ping
    @Test(
        description = "TC-API-001 | GET /ping returns 201 Created",
        groups       = {"smoke", "regression", "functional"}
    )
    @Story("Positive — health check")
    @Severity(SeverityLevel.BLOCKER)
    @Description(
        "Positive scenario — verifies that the /ping endpoint is reachable and " +
        "returns HTTP 201 Created as documented in the Restful Booker spec."
    )
    public void ping_get_returns201Created() {
        Response response = HealthCheckService.ping();

        response.then()
                .assertThat()
                .statusCode(201);

        log.info("[TC-API-001] Status code: {}", response.getStatusCode());
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-002 ─ Positive: response body
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-002 | Endpoint: GET /ping
    @Test(
        description = "TC-API-002 | GET /ping response body equals \"Created\"",
        groups       = {"regression", "functional"}
    )
    @Story("Positive — health check")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Positive scenario — verifies the literal response body is \"Created\" " +
        "(case-sensitive). Source: Restful Booker API spec."
    )
    public void ping_get_responseBodyEqualsCreated() {
        // Expected body value is loaded from test data to keep assertions data-driven
        String expectedBody = (String) pingTestData.get("expectedBody");

        Response response = HealthCheckService.ping();

        response.then()
                .assertThat()
                .statusCode(201)
                .body(equalTo(expectedBody));

        log.info("[TC-API-002] Body: '{}'", response.getBody().asString());
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-003 ─ Positive: Content-Type header
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-003 | Endpoint: GET /ping
    @Test(
        description = "TC-API-003 | GET /ping Content-Type contains \"text/plain\"",
        groups       = {"regression", "functional"}
    )
    @Story("Positive — header validation")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Positive scenario — verifies the Content-Type response header contains " +
        "\"text/plain\" as documented in the Restful Booker API spec."
    )
    public void ping_get_contentTypeContainsTextPlain() {
        String expectedContentType = (String) pingTestData.get("expectedContentType");

        Response response = HealthCheckService.ping();

        response.then()
                .assertThat()
                .statusCode(201)
                .contentType(containsString(expectedContentType));

        log.info("[TC-API-003] Content-Type: '{}'",
                response.getHeader("Content-Type"));
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-004 ─ Performance: response-time SLA
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-004 | Endpoint: GET /ping
    @Test(
        description = "TC-API-004 | GET /ping response time is within the 2 000 ms SLA",
        groups       = {"performance", "regression"}
    )
    @Story("Performance — SLA")
    @Severity(SeverityLevel.CRITICAL)
    @Description(
        "Performance scenario — verifies the end-to-end response time for GET /ping " +
        "is below the configured SLA ceiling (default 2 000 ms). " +
        "SLA is read from config.properties (response.time.sla.ms) — not hardcoded."
    )
    public void ping_get_responseTimeWithinSla() {
        Response response = HealthCheckService.ping();

        response.then()
                .assertThat()
                .statusCode(201)
                .time(lessThan(slaMs), TimeUnit.MILLISECONDS);

        log.info("[TC-API-004] Response time: {}ms (SLA: {}ms)",
                response.getTime(), slaMs);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-005 ─ HEAD request [Inference (low confidence)]
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-005 | Endpoint: HEAD /ping
    // Inference (low confidence) — spec documents GET only. Express 4.x
    // auto-handles HEAD for GET routes, returning the status code with no body.
    @Test(
        description = "TC-API-005 | HEAD /ping returns 201 with an empty body [Inference]",
        groups       = {"regression", "functional"}
    )
    @Story("Positive — HEAD method")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Inference (low confidence) — HEAD /ping is not in the Restful Booker spec. " +
        "Express 4.x mirrors GET routes for HEAD requests, returning the documented " +
        "status code (201) with no response body. Update if live behaviour differs."
    )
    public void ping_head_returns201WithEmptyBody() {
        Response response = HealthCheckService.pingHead();

        response.then()
                .assertThat()
                .statusCode(201);

        // HEAD responses MUST NOT include a message body per RFC 7231 §4.3.2
        String body = response.getBody().asString();
        assertThat("[TC-API-005] HEAD response body must be empty", body,
                blankOrNullString());

        log.info("[TC-API-005] HEAD status: {}, body length: {}",
                response.getStatusCode(), body.length());
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-006 ─ Negative: POST on /ping [Inference (low confidence)]
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-006 | Endpoint: POST /ping
    // Inference (low confidence) — spec documents GET only.
    // Express returns 404 for unregistered routes; update if server returns 405.
    @Test(
        description = "TC-API-006 | POST /ping is an invalid method → non-201 response [Inference]",
        groups       = {"regression", "negative"}
    )
    @Story("Negative — invalid HTTP methods")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Inference (low confidence) — POST is not documented for /ping. " +
        "Asserts the response is not 201 Created (invalid method should not succeed). " +
        "Exact error status (404 / 405) is not asserted to avoid inventing undocumented behaviour."
    )
    public void ping_post_invalidMethodReturnsNon201() {
        Response response = HealthCheckService.pingPost();

        int status = response.getStatusCode();
        assertThat("[TC-API-006] POST /ping must not return 201",
                status, not(equalTo(201)));
        assertThat("[TC-API-006] POST /ping should return a 4xx error",
                status, greaterThanOrEqualTo(400));

        log.info("[TC-API-006] POST /ping status: {}", status);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-007 ─ Negative: PUT on /ping [Inference (low confidence)]
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-007 | Endpoint: PUT /ping
    // Inference (low confidence)
    @Test(
        description = "TC-API-007 | PUT /ping is an invalid method → non-201 response [Inference]",
        groups       = {"regression", "negative"}
    )
    @Story("Negative — invalid HTTP methods")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Inference (low confidence) — PUT is not documented for /ping. " +
        "Asserts the response is not 201 and is a client error (4xx)."
    )
    public void ping_put_invalidMethodReturnsNon201() {
        Response response = HealthCheckService.pingPut();

        int status = response.getStatusCode();
        assertThat("[TC-API-007] PUT /ping must not return 201",
                status, not(equalTo(201)));
        assertThat("[TC-API-007] PUT /ping should return a 4xx error",
                status, greaterThanOrEqualTo(400));

        log.info("[TC-API-007] PUT /ping status: {}", status);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-008 ─ Negative: DELETE on /ping [Inference (low confidence)]
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-008 | Endpoint: DELETE /ping
    // Inference (low confidence)
    @Test(
        description = "TC-API-008 | DELETE /ping is an invalid method → non-201 response [Inference]",
        groups       = {"regression", "negative"}
    )
    @Story("Negative — invalid HTTP methods")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Inference (low confidence) — DELETE is not documented for /ping. " +
        "Asserts the response is not 201 and is a client error (4xx)."
    )
    public void ping_delete_invalidMethodReturnsNon201() {
        Response response = HealthCheckService.pingDelete();

        int status = response.getStatusCode();
        assertThat("[TC-API-008] DELETE /ping must not return 201",
                status, not(equalTo(201)));
        assertThat("[TC-API-008] DELETE /ping should return a 4xx error",
                status, greaterThanOrEqualTo(400));

        log.info("[TC-API-008] DELETE /ping status: {}", status);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-009 ─ Negative: PATCH on /ping [Inference (low confidence)]
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-009 | Endpoint: PATCH /ping
    // Inference (low confidence)
    @Test(
        description = "TC-API-009 | PATCH /ping is an invalid method → non-201 response [Inference]",
        groups       = {"regression", "negative"}
    )
    @Story("Negative — invalid HTTP methods")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Inference (low confidence) — PATCH is not documented for /ping. " +
        "Asserts the response is not 201 and is a client error (4xx)."
    )
    public void ping_patch_invalidMethodReturnsNon201() {
        Response response = HealthCheckService.pingPatch();

        int status = response.getStatusCode();
        assertThat("[TC-API-009] PATCH /ping must not return 201",
                status, not(equalTo(201)));
        assertThat("[TC-API-009] PATCH /ping should return a 4xx error",
                status, greaterThanOrEqualTo(400));

        log.info("[TC-API-009] PATCH /ping status: {}", status);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-010 ─ Positive: idempotency (3 consecutive hits)
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-010 | Endpoint: GET /ping (repeated calls)
    @Test(
        description = "TC-API-010 | GET /ping is idempotent — 3 consecutive hits all return 201",
        groups       = {"regression", "functional"}
    )
    @Story("Positive — idempotency")
    @Severity(SeverityLevel.NORMAL)
    @Description(
        "Positive scenario — calls GET /ping three times in sequence and asserts " +
        "each call returns 201 Created.  Verifies the endpoint is stateless and " +
        "idempotent as expected for a health-check resource."
    )
    public void ping_get_threeConsecutiveHitsAllReturn201() {
        final int hitCount = 3;

        for (int i = 1; i <= hitCount; i++) {
            Response response = HealthCheckService.ping();

            response.then()
                    .assertThat()
                    .statusCode(201)
                    .body(equalTo("Created"));

            log.info("[TC-API-010] Hit #{}: status={}, body='{}'",
                    i, response.getStatusCode(), response.getBody().asString());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-011 ─ Negative: malformed Accept header [Inference (low confidence)]
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-011 | Endpoint: GET /ping
    // Inference (low confidence) — the spec does not document Accept-header
    // negotiation behaviour. Standard HTTP servers typically ignore unrecognised
    // Accept values and respond with their default Content-Type.
    @Test(
        description = "TC-API-011 | GET /ping with unrecognised Accept header still returns 201 [Inference]",
        groups       = {"regression", "negative"}
    )
    @Story("Negative — malformed request headers")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Inference (low confidence) — sends a deliberately unrecognised MIME type " +
        "in the Accept header (application/vnd.unknown+xml) and asserts the server " +
        "still returns 201 Created.  The Restful Booker spec does not document " +
        "content-negotiation behaviour; update this test if the live server returns 406."
    )
    public void ping_get_withUnrecognisedAcceptHeader_returns201() {
        // Deliberately unrecognised MIME — not in the spec; for boundary coverage only
        final String malformedAccept = "application/vnd.unknown.format+xml;version=99";

        Response response = HealthCheckService.pingWithCustomAccept(malformedAccept);

        // Inference: server ignores bad Accept and returns its default 201 response
        response.then()
                .assertThat()
                .statusCode(201);

        log.info("[TC-API-011] Accept: '{}' → status: {}", malformedAccept,
                response.getStatusCode());
    }

    // ════════════════════════════════════════════════════════════════════════
    // TC-API-012 ─ Negative: unreachable host
    // ════════════════════════════════════════════════════════════════════════

    // Requirement: REQ-HC-012 | Endpoint: GET /ping (unreachable base URI)
    @Test(
        description = "TC-API-012 | GET /ping against a non-existent host raises a connectivity exception",
        groups       = {"regression", "negative"}
    )
    @Story("Negative — network failure")
    @Severity(SeverityLevel.MINOR)
    @Description(
        "Negative scenario — sends GET /ping to a deliberately non-existent hostname. " +
        "REST Assured wraps the underlying java.net.UnknownHostException / " +
        "ConnectException in a RuntimeException. " +
        "Asserts that a non-null exception is thrown (no response is possible)."
    )
    public void ping_get_unreachableHost_throwsConnectivityException() {
        // Non-routable test domain — chosen to guarantee DNS failure
        final String unreachableUri =
                "https://restful-booker-does-not-exist-12345.example.invalid";

        RuntimeException caughtException = null;

        try {
            HealthCheckService.pingUnreachable(unreachableUri);
            // If execution reaches here, the call somehow succeeded — test must fail
            Assert.fail("[TC-API-012] Expected a connectivity exception for unreachable " +
                        "host '" + unreachableUri + "' but a response was received.");
        } catch (RuntimeException ex) {
            caughtException = ex;
            log.info("[TC-API-012] Expected exception caught: {} — {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }

        assertThat("[TC-API-012] A RuntimeException must be thrown for unreachable host",
                caughtException, notNullValue());
    }
}
