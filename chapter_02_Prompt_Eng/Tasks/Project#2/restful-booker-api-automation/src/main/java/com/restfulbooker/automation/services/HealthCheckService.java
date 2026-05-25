package com.restfulbooker.automation.services;

import com.restfulbooker.automation.client.RestClient;
import com.restfulbooker.automation.client.SpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service Object for the Restful Booker Health Check endpoint.
 *
 * <p><b>Endpoint reference</b><br>
 * Source: <a href="https://restful-booker.herokuapp.com/apidoc/index.html#api-Ping-Ping">
 *     Restful Booker API Docs — Ping</a>
 *
 * <table border="1">
 *   <tr><th>Method</th><th>Path</th><th>Status</th><th>Body</th><th>Content-Type</th></tr>
 *   <tr><td>GET</td><td>/ping</td><td>201 Created</td><td>Created</td>
 *       <td>text/plain; charset=utf-8</td></tr>
 * </table>
 *
 * <p>Every method in this class returns the raw {@link Response} so callers can
 * chain REST Assured assertions freely.  No assertion logic lives here.
 *
 * <p>Methods that exercise undocumented HTTP verbs (HEAD, POST, PUT, DELETE, PATCH)
 * are needed for negative / boundary tests.  Each such method is tagged with
 * <em>Inference (low confidence)</em> — the Restful Booker spec documents only
 * {@code GET /ping}.
 */
public final class HealthCheckService {

    private static final Logger log = LogManager.getLogger(HealthCheckService.class);

    /** Relative path documented in the spec. */
    public static final String PING_PATH = "/ping";

    private HealthCheckService() {}

    // ── Documented operation ─────────────────────────────────────────────────

    /**
     * Sends {@code GET /ping}.
     * Expected by spec: 201 Created, body "Created", Content-Type text/plain.
     *
     * @return raw REST Assured {@link Response}
     */
    public static Response ping() {
        log.info("HealthCheckService.ping() → GET {}", PING_PATH);
        RequestSpecification spec = SpecBuilder.buildPlainTextRequestSpec();
        return RestClient.get(PING_PATH, spec);
    }

    // ── Inferred / negative operations ──────────────────────────────────────

    /**
     * Sends {@code HEAD /ping}.
     * <em>Inference (low confidence)</em> — the spec documents GET only.
     * Express 4.x auto-mirrors GET routes for HEAD, so 201 with an empty body
     * is the expected outcome; this is not guaranteed by the published spec.
     *
     * @return raw REST Assured {@link Response}
     */
    public static Response pingHead() {
        log.info("HealthCheckService.pingHead() → HEAD {}", PING_PATH);
        RequestSpecification spec = SpecBuilder.buildBaseRequestSpec();
        return RestClient.head(PING_PATH, spec);
    }

    /**
     * Sends {@code POST /ping} (intentionally invalid method — negative test).
     * <em>Inference (low confidence)</em> — Express returns 404 for unregistered routes.
     *
     * @return raw REST Assured {@link Response}
     */
    public static Response pingPost() {
        log.info("HealthCheckService.pingPost() → POST {}", PING_PATH);
        RequestSpecification spec = SpecBuilder.buildBaseRequestSpec();
        return RestClient.post(PING_PATH, spec);
    }

    /**
     * Sends {@code PUT /ping} (intentionally invalid method — negative test).
     * <em>Inference (low confidence)</em>
     *
     * @return raw REST Assured {@link Response}
     */
    public static Response pingPut() {
        log.info("HealthCheckService.pingPut() → PUT {}", PING_PATH);
        RequestSpecification spec = SpecBuilder.buildBaseRequestSpec();
        return RestClient.put(PING_PATH, spec);
    }

    /**
     * Sends {@code DELETE /ping} (intentionally invalid method — negative test).
     * <em>Inference (low confidence)</em>
     *
     * @return raw REST Assured {@link Response}
     */
    public static Response pingDelete() {
        log.info("HealthCheckService.pingDelete() → DELETE {}", PING_PATH);
        RequestSpecification spec = SpecBuilder.buildBaseRequestSpec();
        return RestClient.delete(PING_PATH, spec);
    }

    /**
     * Sends {@code PATCH /ping} (intentionally invalid method — negative test).
     * <em>Inference (low confidence)</em>
     *
     * @return raw REST Assured {@link Response}
     */
    public static Response pingPatch() {
        log.info("HealthCheckService.pingPatch() → PATCH {}", PING_PATH);
        RequestSpecification spec = SpecBuilder.buildBaseRequestSpec();
        return RestClient.patch(PING_PATH, spec);
    }

    /**
     * Sends {@code GET /ping} with a caller-supplied {@code Accept} header.
     * Used by negative tests that submit unrecognised / malformed MIME types.
     * <em>Inference (low confidence)</em> — expected: server ignores bad Accept and
     * still returns 201.
     *
     * @param acceptHeader  value to set for the {@code Accept} request header
     * @return raw REST Assured {@link Response}
     */
    public static Response pingWithCustomAccept(String acceptHeader) {
        log.info("HealthCheckService.pingWithCustomAccept() → GET {} (Accept: {})",
                PING_PATH, acceptHeader);
        RequestSpecification spec = SpecBuilder.buildCustomAcceptSpec(acceptHeader);
        return RestClient.get(PING_PATH, spec);
    }

    /**
     * Sends {@code GET /ping} against an unreachable host.
     * Used by the negative connectivity test (REQ-HC-012).
     * REST Assured wraps the underlying {@code UnknownHostException} /
     * {@code ConnectException} in a {@link RuntimeException}.
     *
     * @param unreachableBaseUri  deliberately non-existent base URI
     * @return raw REST Assured {@link Response} (should never be reached;
     *         the caller is expected to catch the exception)
     */
    public static Response pingUnreachable(String unreachableBaseUri) {
        log.info("HealthCheckService.pingUnreachable() → GET {} (base={})",
                PING_PATH, unreachableBaseUri);
        RequestSpecification spec = SpecBuilder.buildUnreachableHostSpec(unreachableBaseUri);
        return RestClient.get(PING_PATH, spec);
    }
}
