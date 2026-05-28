package com.restfulbooker.api.clients;

import com.restfulbooker.api.constants.Endpoints;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * API client for GET /ping.
 */
public class PingClient {

    /** Calls GET /ping and returns the raw response. */
    public Response ping() {
        return given()
                .spec(RequestSpecFactory.getBaseSpec())
                .when()
                .get(Endpoints.PING)
                .then()
                .extract().response();
    }
}
