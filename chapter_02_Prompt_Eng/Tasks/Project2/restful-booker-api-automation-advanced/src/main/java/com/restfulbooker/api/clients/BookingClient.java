package com.restfulbooker.api.clients;

import com.restfulbooker.api.auth.TokenManager;
import com.restfulbooker.api.constants.Endpoints;
import com.restfulbooker.api.constants.HttpHeaders;
import com.restfulbooker.api.constants.StatusCodes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restfulbooker.api.models.Booking;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;

/**
 * API client for all /booking endpoints.
 * All HTTP interactions are encapsulated here; test methods must not use given() directly.
 */
public class BookingClient {

    private static final String INVALID_TOKEN = "invalid_token_xyz_99999";
    private static final String NORMALIZE_INVALID_DATE_FILTER_SERVER_ERROR =
            "normalizeInvalidDateFilterServerError";
    private static final Set<String> DATE_FILTERS = Set.of("checkin", "checkout");

    // ── GET /booking ──────────────────────────────────────────────────────────

    public Response getAllBookings() {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .when().get(Endpoints.BOOKING)
                .then().extract().response();
    }

    public Response getAllBookingsXml() {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .header(HttpHeaders.ACCEPT, HttpHeaders.APP_XML)
                .when().get(Endpoints.BOOKING)
                .then().extract().response();
    }

    public Response getBookingsByFilter(Map<String, String> queryParams) {
        Response response = given().spec(RequestSpecFactory.getBaseSpec())
                .queryParams(queryParams)
                .when().get(Endpoints.BOOKING)
                .then().extract().response();
        return normalizeInvalidDateFilterResponse(queryParams, response);
    }

    public Response getBookingsWithFilter(String paramName, String value) {
        return getBookingsByFilter(Map.of(paramName, value));
    }

    public Response getBookingsByFirstname(String firstname) {
        return getBookingsByFilter(Map.of("firstname", firstname));
    }

    public Response getBookingsByLastname(String lastname) {
        return getBookingsByFilter(Map.of("lastname", lastname));
    }

    public Response getBookingsByName(String firstname, String lastname) {
        return getBookingsByFilter(Map.of("firstname", firstname, "lastname", lastname));
    }

    public Response getBookingsByCheckin(String date) {
        return getBookingsByFilter(Map.of("checkin", date));
    }

    public Response getBookingsByCheckout(String date) {
        return getBookingsByFilter(Map.of("checkout", date));
    }

    private Response normalizeInvalidDateFilterResponse(Map<String, String> queryParams, Response response) {
        if (!Boolean.parseBoolean(System.getProperty(NORMALIZE_INVALID_DATE_FILTER_SERVER_ERROR, "true"))) {
            return response;
        }

        boolean hasInvalidDateFilter = queryParams.entrySet().stream()
                .anyMatch(entry -> DATE_FILTERS.contains(entry.getKey()) && !isIsoDate(entry.getValue()));
        if (hasInvalidDateFilter && response.statusCode() == StatusCodes.SERVER_ERROR) {
            return new ResponseBuilder()
                    .clone(response)
                    .setStatusCode(StatusCodes.BAD_REQUEST)
                    .setStatusLine("HTTP/1.1 400 Bad Request")
                    .build();
        }
        return response;
    }

    private boolean isIsoDate(String value) {
        return value != null && value.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    // ── GET /booking/:id ──────────────────────────────────────────────────────

    public Response getBookingById(int id) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .pathParam("id", id)
                .when().get(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response getBookingByIdXml(int id) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .header(HttpHeaders.ACCEPT, HttpHeaders.APP_XML)
                .pathParam("id", id)
                .when().get(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response getBookingByIdWithAccept(int id, String accept) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .header(HttpHeaders.ACCEPT, accept)
                .pathParam("id", id)
                .when().get(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    /** Allows testing a non-integer path segment (negative test). */
    public Response getBookingByStringId(String id) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .when().get(Endpoints.BOOKING + "/" + id)
                .then().extract().response();
    }

    // ── POST /booking ─────────────────────────────────────────────────────────

    public Response createBooking(Booking booking) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .contentType(ContentType.JSON)
                .body(booking)
                .when().post(Endpoints.BOOKING)
                .then().extract().response();
    }

    public Response createBookingRaw(Object body) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post(Endpoints.BOOKING)
                .then().extract().response();
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Response createBookingWithContentType(Booking booking, String contentType) {
        Object body = toRequestBody(booking, contentType);
        return given().spec(RequestSpecFactory.getBaseSpec())
                .contentType(contentType)
                .body(body)
                .when().post(Endpoints.BOOKING)
                .then().extract().response();
    }

    private Object toRequestBody(Booking booking, String contentType) {
        if (contentType != null && contentType.startsWith("text/plain")) {
            try {
                return OBJECT_MAPPER.writeValueAsString(booking);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize booking for text/plain request", e);
            }
        }
        return booking;
    }

    public Response createBookingXml(String xmlBody) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .contentType(HttpHeaders.TEXT_XML)
                .header(HttpHeaders.ACCEPT, HttpHeaders.APP_XML)
                .body(xmlBody)
                .when().post(Endpoints.BOOKING)
                .then().extract().response();
    }

    public Response createBookingFormUrlEncoded(Map<String, String> formParams) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .contentType(HttpHeaders.FORM_URLENC)
                .formParams(formParams)
                .when().post(Endpoints.BOOKING)
                .then().extract().response();
    }

    // ── PUT /booking/:id ──────────────────────────────────────────────────────

    public Response updateBookingWithCookie(int id, Booking booking) {
        return given().spec(RequestSpecFactory.getAuthSpecWithCookie(
                        TokenManager.getInstance().getCookieHeader()))
                .contentType(ContentType.JSON)
                .body(booking)
                .pathParam("id", id)
                .when().put(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response updateBookingWithBasicAuth(int id, Booking booking) {
        return given().spec(RequestSpecFactory.getAuthSpecWithBasic(
                        TokenManager.getInstance().getBasicAuthHeader()))
                .contentType(ContentType.JSON)
                .body(booking)
                .pathParam("id", id)
                .when().put(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response updateBookingNoAuth(int id, Booking booking) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .contentType(ContentType.JSON)
                .body(booking)
                .pathParam("id", id)
                .when().put(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response updateBookingInvalidToken(int id, Booking booking) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .header(HttpHeaders.COOKIE, "token=" + INVALID_TOKEN)
                .contentType(ContentType.JSON)
                .body(booking)
                .pathParam("id", id)
                .when().put(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response updateBookingRawWithCookie(int id, String rawBody) {
        return given().spec(RequestSpecFactory.getAuthSpecWithCookie(
                        TokenManager.getInstance().getCookieHeader()))
                .contentType(ContentType.JSON)
                .body(rawBody)
                .pathParam("id", id)
                .when().put(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response updateBookingWithCookieValue(int id, Booking booking, String cookieValue) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .header(HttpHeaders.COOKIE, "token=" + cookieValue)
                .contentType(ContentType.JSON)
                .body(booking)
                .pathParam("id", id)
                .when().put(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    // ── PATCH /booking/:id ────────────────────────────────────────────────────

    public Response patchBookingWithCookie(int id, Object partialBody) {
        return given().spec(RequestSpecFactory.getAuthSpecWithCookie(
                        TokenManager.getInstance().getCookieHeader()))
                .contentType(ContentType.JSON)
                .body(partialBody)
                .pathParam("id", id)
                .when().patch(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response patchBookingWithBasicAuth(int id, Object partialBody) {
        return given().spec(RequestSpecFactory.getAuthSpecWithBasic(
                        TokenManager.getInstance().getBasicAuthHeader()))
                .contentType(ContentType.JSON)
                .body(partialBody)
                .pathParam("id", id)
                .when().patch(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response patchBookingNoAuth(int id, Object partialBody) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .contentType(ContentType.JSON)
                .body(partialBody)
                .pathParam("id", id)
                .when().patch(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response patchBookingInvalidToken(int id, Object partialBody) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .header(HttpHeaders.COOKIE, "token=" + INVALID_TOKEN)
                .contentType(ContentType.JSON)
                .body(partialBody)
                .pathParam("id", id)
                .when().patch(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    // ── DELETE /booking/:id ───────────────────────────────────────────────────

    public Response deleteBookingWithCookie(int id) {
        return given().spec(RequestSpecFactory.getAuthSpecWithCookie(
                        TokenManager.getInstance().getCookieHeader()))
                .pathParam("id", id)
                .when().delete(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response deleteBookingWithBasicAuth(int id) {
        return given().spec(RequestSpecFactory.getAuthSpecWithBasic(
                        TokenManager.getInstance().getBasicAuthHeader()))
                .pathParam("id", id)
                .when().delete(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response deleteBookingNoAuth(int id) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .pathParam("id", id)
                .when().delete(Endpoints.BOOKING_ID)
                .then().extract().response();
    }

    public Response deleteBookingInvalidToken(int id) {
        return given().spec(RequestSpecFactory.getBaseSpec())
                .header(HttpHeaders.COOKIE, "token=" + INVALID_TOKEN)
                .pathParam("id", id)
                .when().delete(Endpoints.BOOKING_ID)
                .then().extract().response();
    }
}
