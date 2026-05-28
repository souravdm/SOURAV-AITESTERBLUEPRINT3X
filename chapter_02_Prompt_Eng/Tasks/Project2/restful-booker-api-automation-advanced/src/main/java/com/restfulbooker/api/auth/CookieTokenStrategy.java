package com.restfulbooker.api.auth;

import com.restfulbooker.api.config.ConfigManager;
import com.restfulbooker.api.constants.Endpoints;
import com.restfulbooker.api.exceptions.AuthException;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static io.restassured.RestAssured.given;

/**
 * Acquires and caches a Cookie token via POST /auth.
 * Thread-safe: uses a ReentrantLock to prevent concurrent refresh races.
 */
public class CookieTokenStrategy implements AuthStrategy {

    private static final Logger log = LoggerFactory.getLogger(CookieTokenStrategy.class);

    private final ConfigManager config = ConfigManager.getInstance();
    private final ReentrantLock lock   = new ReentrantLock();

    private volatile String cachedToken;

    @Override
    public String getToken() {
        if (cachedToken == null) {
            refresh();
        }
        return cachedToken;
    }

    @Override
    public void refresh() {
        lock.lock();
        try {
            log.info("Refreshing Cookie token for env={}", config.getEnv());
            String baseUrl  = config.getRequired("base.url");
            String username = config.getRequired("auth.username");
            String password = config.getRequired("auth.password");

            Response resp = given()
                    .baseUri(baseUrl)
                    .contentType(ContentType.JSON)
                    .body(Map.of("username", username, "password", password))
                    .when()
                    .post(Endpoints.AUTH)
                    .then()
                    .extract().response();

            if (resp.statusCode() != 200) {
                throw new AuthException("Token acquisition failed: HTTP " + resp.statusCode()
                        + " — " + resp.body().asString());
            }

            String token = resp.jsonPath().getString("token");
            if (token == null || token.isBlank()) {
                throw new AuthException("Auth response did not contain a token: " + resp.body().asString());
            }
            cachedToken = token;
            log.info("Token acquired successfully");
        } finally {
            lock.unlock();
        }
    }
}
