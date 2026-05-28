package com.restfulbooker.api.tests.smoke;

import com.restfulbooker.api.clients.PingClient;
import com.restfulbooker.api.constants.StatusCodes;
import com.restfulbooker.api.tests.base.BaseTest;
import com.restfulbooker.api.utils.AssertionHelper;
import io.restassured.response.Response;
import org.testng.annotations.Test;

/**
 * Smoke tests for GET /ping.
 * Test Plan scenarios: TS-PG-001, TS-PG-003
 */
public class PingTests extends BaseTest {

    private final PingClient pingClient = new PingClient();

    @Test(
        groups  = {"smoke", "ping"},
        description = "TC_PING_001 | TS-PG-001 — GET /ping returns HTTP 201 Created (documented status)"
    )
    public void tc_ping_001_healthCheckReturns201() {
        Response response = pingClient.ping();
        // PRD example shows HTTP 201 Created; accept either 200 or 201 (OQ-03 open)
        int status = response.statusCode();
        org.assertj.core.api.Assertions.assertThat(status)
                .as("GET /ping expected 200 or 201 but got %d", status)
                .isIn(StatusCodes.OK, StatusCodes.CREATED);
    }

    @Test(
        groups  = {"smoke", "ping"},
        description = "TC_PING_002 | TS-PG-003 — GET /ping responds within 5-second SLA"
    )
    public void tc_ping_002_healthCheckResponseTimeWithinSla() {
        Response response = pingClient.ping();
        AssertionHelper.assertResponseTimeSla(response);
    }
}
