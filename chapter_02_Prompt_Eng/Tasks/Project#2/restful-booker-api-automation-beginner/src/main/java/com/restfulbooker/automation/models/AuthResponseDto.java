package com.restfulbooker.automation.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for {@code POST /auth}.
 *
 * <p>Source: <a href="https://restful-booker.herokuapp.com/apidoc/index.html#api-Auth-CreateToken">
 *     Restful Booker API Docs — CreateToken</a>
 *
 * <pre>
 * {
 *   "token": "abc123"
 * }
 * </pre>
 *
 * The token value is an opaque string; its format is not documented by the spec.
 * Do not assert on its internal structure — only assert that it is non-null and
 * non-empty.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponseDto {

    @JsonProperty("token")
    private String token;

    // ── Constructors ─────────────────────────────────────────────────────────

    public AuthResponseDto() {}

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
