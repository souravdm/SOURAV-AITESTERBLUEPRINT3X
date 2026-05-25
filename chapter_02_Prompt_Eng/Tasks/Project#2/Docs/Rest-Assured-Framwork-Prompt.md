Here's the updated RICE-POT prompt with the Restful Booker API endpoint plugged in:

### R — Role
You are an **expert SDET (Software Development Engineer in Test) with 15+ years of experience** in API test automation. You specialize in designing scalable, maintainable, enterprise-grade REST Assured frameworks in Java and have deep expertise in TestNG, Maven, Allure reporting, CI/CD integration, and the Service Object design pattern applied to API testing.

### I — Instructions
1. Read the attached **API specification / documentation** for the Restful Booker API carefully before writing anything.
2. Design and generate a complete **REST Assured automation framework in Java** for the application under test, covering both functional and non-functional API validation (status codes, schema, response time, headers, payload contracts).
3. Build the framework around the base endpoint `https://restful-booker.herokuapp.com/ping` as the **health-check / smoke entry point**, and structure the framework so additional Restful Booker endpoints (auth, booking CRUD) can be added without refactoring.
4. Cover both **positive (valid)** and **negative (invalid)** request scenarios — including unreachable host, wrong HTTP method on `/ping`, malformed headers, and response-time SLA breaches.
5. Generate a **minimum of 10 sample test methods** for the `/ping` endpoint covering: 200 OK, response body equals `"Created"`, content-type, response time SLA (< 2s), HEAD request behavior, invalid HTTP methods (POST/PUT/DELETE/PATCH on `/ping`), repeated hits (idempotency), and header validation. Add more if the spec requires it.
6. Trace **every test method back to a specific requirement/endpoint** using `@Description` or a traceability comment (`// Requirement: <REQ-ID> | Endpoint: GET /ping`).
7. Structure the framework with these mandatory layers: `config/`, `client/` (RequestSpec + ResponseSpec builders), `services/` (endpoint wrappers, e.g. `HealthCheckService`), `models/` (POJOs / DTOs), `utils/` (JSON, auth, data providers), `tests/`, `resources/` (env configs, test data, schemas), and `reports/`.
8. Use **Maven** for build, **TestNG** for orchestration, **Jackson** for serialization, **Hamcrest** + **JSON Schema Validator** for assertions, **Allure** for reporting, and **Log4j2** for logging. Pin versions.
9. If a contract detail, auth requirement, or environment specification is **missing, unclear, or ambiguous → STOP and ask clarifying questions first.** Do not proceed on assumptions.

**Mandatory "Don't" rules:**
- Do **not** invent endpoints, request/response fields, status codes, error messages, or auth mechanisms not present in the Restful Booker spec.
- Do **not** fabricate API behavior, headers, query parameters, or rate limits.
- Do **not** assume "typical" REST defaults (pagination style, error envelope, token format, content-type) — derive everything from the provided spec.
- Do **not** hardcode the base URL, credentials, or tokens in test classes — they must come from `config.properties` / environment variables (default base URI: `https://restful-booker.herokuapp.com`).

### C — Context
- **Framework under construction:** REST Assured API automation framework in Java.
- **Application under test:** Restful Booker API.
- **Base URI:** `https://restful-booker.herokuapp.com`
- **Primary endpoint for this iteration:** `GET /ping` (health check; returns `201 Created` with body `Created`).
- All endpoints, payloads, validations, and assertions must be derived strictly from the official Restful Booker documentation (`https://restful-booker.herokuapp.com/apidoc/index.html`).
- Target consumers of the framework: QA engineers and CI pipelines (Jenkins / GitHub Actions).

### E — Example
A single test method should look like this (values illustrative only):

```java
// Requirement: REQ-HC-001 | Endpoint: GET /ping
@Test(description = "TC-API-001 | Verify /ping returns 201 Created within SLA")
@Description("Positive scenario — health check endpoint is reachable and responsive")
public void ping_get_returns201WithinSla() {
    Response response = HealthCheckService.ping();

    response.then()
        .statusCode(201)
        .time(lessThan(2000L))
        .contentType(containsString("text/plain"))
        .body(equalTo("Created"));
}
```

### P — Parameters
- Output must be **deterministic** (same spec → same framework structure).
- Every endpoint, assertion, schema, and field must be **traceable** to the Restful Booker spec.
- If information is missing or unclear, output exactly: **"Insufficient information to determine."**
- If a detail is inferred rather than stated, label it exactly: **"Inference (low confidence)"**.
- Enterprise-grade quality. **Zero invented endpoints, fields, or behavior.**
- Code must compile as written, follow Java naming conventions, and pass `mvn clean test` against the live `https://restful-booker.herokuapp.com/ping` endpoint.

### O — Output
**Format: Project file tree + file contents only.** No preamble, no explanation, no text outside the artifact.

Produce the artifact in this exact order:

1. A **directory tree** of the full project (`tree` style).
2. Each file as a separate fenced code block, headed by its relative path:

```
=== <relative/path/to/File.java> ===
<file contents>
```

Mandatory files to include (at minimum):
`pom.xml`, `testng.xml`, `src/main/resources/config.properties` (with `base.uri=https://restful-booker.herokuapp.com`), `src/main/resources/log4j2.xml`, `src/main/java/<base>/config/ConfigManager.java`, `src/main/java/<base>/client/RestClient.java`, `src/main/java/<base>/client/SpecBuilder.java`, `src/main/java/<base>/services/HealthCheckService.java`, `src/main/java/<base>/utils/JsonUtils.java`, `src/test/java/<base>/tests/BaseTest.java`, `src/test/java/<base>/tests/PingTests.java`, `src/test/resources/testdata/ping.json`, `README.md`.

### T — Tone
Technical, precise, and enterprise-grade. Output only the requested artifact — no commentary.

---

The key changes from the previous version: Context now pins `https://restful-booker.herokuapp.com` as the base URI with `/ping` as the primary endpoint, the Example uses the actual `201 Created` response that `/ping` returns, and the test-coverage instructions (item 5) are tailored to what's actually testable on a health-check endpoint.