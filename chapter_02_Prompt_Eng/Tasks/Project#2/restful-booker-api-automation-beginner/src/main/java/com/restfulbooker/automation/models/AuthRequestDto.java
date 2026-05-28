package com.restfulbooker.automation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body DTO for {@code POST /auth}.
 *
 * <p>Source: <a href="https://restful-booker.herokuapp.com/apidoc/index.html#api-Auth-CreateToken">
 *     Restful Booker API Docs — CreateToken</a>
 *
 * <pre>
 * {
 *   "username": "admin",
 *   "password": "password123"
 * }
 * </pre>
 *
 * Credentials must be injected from {@link com.restfulbooker.automation.config.ConfigManager}
 * — never hardcode them in test classes.
 */
public class AuthRequestDto {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    // ── Constructors ─────────────────────────────────────────────────────────

    public AuthRequestDto() {}

    public AuthRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
