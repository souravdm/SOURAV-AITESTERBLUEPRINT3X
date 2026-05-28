package com.restfulbooker.api.clients;

import com.restfulbooker.api.constants.Endpoints;
import com.restfulbooker.api.models.AuthRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * API client for the Auth module (POST /auth).
 */
public class AuthClient {

    /**
     * Posts credentials to /auth and returns the raw response.
     *
     * @param request body containing username and password
     * @return raw REST Assured Response
     */
    public Response createToken(AuthRequest request) {
        return given()
                .spec(RequestSpecFactory.getBaseSpec())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(Endpoints.AUTH)
                .then()
                .extract().response();
    }

    /**
     * Posts credentials as a plain map (useful for negative/missing-field tests).
     *
     * @param body raw object to serialize
     * @return raw Response
     */
    public Response createTokenRaw(Object body) {
        return given()
                .spec(RequestSpecFactory.getBaseSpec())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(Endpoints.AUTH)
                .then()
                .extract().response();
    }

    /**
     * Posts with a custom Content-Type to test content-negotiation.
     */
    public Response createTokenWithContentType(AuthRequest request, String contentType) {
        return given()
                .spec(RequestSpecFactory.getBaseSpec())
                .contentType(contentType)
                .body(request.getUsername() + ":" + request.getPassword())
                .when()
                .post(Endpoints.AUTH)
                .then()
                .extract().response();
    }
}
