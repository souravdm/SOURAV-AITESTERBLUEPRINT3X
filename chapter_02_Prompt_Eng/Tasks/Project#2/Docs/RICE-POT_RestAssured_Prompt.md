# RICE-POT Prompt Template
*REST Assured Framework in Java — Restful Booker /ping Endpoint*

A worked example of the RICE-POT prompt framework for generating an enterprise-grade API automation framework. Copy the prompt section into your AI tool of choice.

---

## Quick Reference: What RICE-POT Means

| Letter | Component | What Goes Here |
|---|---|---|
| **R** | Role | The persona the AI adopts |
| **I** | Instructions | Step-by-step commands + mandatory rules and "Don't" lists |
| **C** | Context | Background — the why and where |
| **E** | Example | A sample row/format that guides the output style |
| **P** | Parameters | Quality, accuracy, and style constraints |
| **O** | Output | The exact artifact and format to produce |
| **T** | Tone | Communication style |

---

## Endpoint Under Test

| Field | Value |
|---|---|
| **Application** | Restful Booker API |
| **Base URI** | https://restful-booker.herokuapp.com |
| **Primary Endpoint** | GET /ping |
| **Expected Status** | 201 Created |
| **Expected Body** | `Created` |
| **Response Time SLA** | < 2000 ms |
| **API Docs** | https://restful-booker.herokuapp.com/apidoc/index.html |

---

## Mandatory Tech Stack

| Layer | Tool / Library | Version |
|---|---|---|
| **Build** | Maven | 3.9+ |
| **Language** | Java | 17 |
| **HTTP/API Library** | REST Assured | 5.4.0 |
| **Test Runner** | TestNG | 7.10.x |
| **Serialization** | Jackson Databind | 2.17.x |
| **Assertions** | Hamcrest + JSON Schema Validator | — |
| **Reporting** | Allure TestNG | 2.27.x |
| **Logging** | Log4j2 | 2.23.x |

---

## The Prompt *(copy from here)*

### R — Role

You are an **expert SDET (Software Development Engineer in Test) with 15+ years of experience** in API test automation. You specialize in designing scalable, maintainable, enterprise-grade REST Assured frameworks in Java and have deep expertise in TestNG, Maven, Allure reporting, CI/CD integration, and the Service Object design pattern applied to API testing.

---

### I — Instructions

- Read the attached **API specification / documentation** for the Restful Booker API carefully before writing anything.

- Design and generate a complete **REST Assured automation framework in Java**, covering both functional and non-functional API validation (status codes, schema, response time, headers, payload contracts).

- Build the framework around the base endpoint `https://restful-booker.herokuapp.com/ping` as the **health-check / smoke entry point**, and structure the framework so additional endpoints (auth, booking CRUD) can be added without refactoring.

- Cover both **positive (valid)** and **invalid (negative)** scenarios — including unreachable host, wrong HTTP method on /ping, malformed headers, and response-time SLA breaches.

- Generate a **minimum of 10 sample test methods** for the /ping endpoint covering: 200/201 status, body equals "Created", content-type, response time SLA (< 2s), HEAD request behavior, invalid HTTP methods (POST/PUT/DELETE/PATCH on /ping), repeated hits (idempotency), and header validation.

- Trace **every test method back to a specific requirement** using `@Description` or a traceability comment (`// Requirement: <REQ-ID> | Endpoint: GET /ping`).

- Structure the framework with these mandatory layers: `config/`, `client/`, `services/`, `models/`, `utils/`, `tests/`, `resources/`, and `reports/`.

- Use **Maven, TestNG, Jackson, Hamcrest, JSON Schema Validator, Allure, and Log4j2**. Pin versions in `pom.xml`.

- If a contract detail, auth requirement, or environment specification is **missing, unclear, or ambiguous → STOP and ask clarifying questions first.** Do not proceed on assumptions.

#### Mandatory "Don't" Rules

- Do **not** invent endpoints, request/response fields, status codes, error messages, or auth mechanisms not present in the Restful Booker spec.
- Do **not** fabricate API behavior, headers, query parameters, or rate limits.
- Do **not** assume "typical" REST defaults (pagination style, error envelope, token format, content-type) — derive everything from the provided spec.
- Do **not** hardcode the base URL, credentials, or tokens in test classes — they must come from `config.properties` / environment variables.

---

### C — Context

- **Framework under construction:** REST Assured API automation framework in Java.
- **Application under test:** Restful Booker API.
- **Base URI:** https://restful-booker.herokuapp.com
- **Primary endpoint for this iteration:** GET /ping (health check; returns 201 Created with body `Created`).
- All endpoints, payloads, validations, and assertions must be derived strictly from the official Restful Booker documentation.
- Target consumers of the framework: QA engineers and CI pipelines (Jenkins / GitHub Actions).

---

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

---

### P — Parameters

- Output must be **deterministic** (same spec → same framework structure).
- Every endpoint, assertion, schema, and field must be **traceable** to the Restful Booker spec.
- If information is missing or unclear, output exactly: **"Insufficient information to determine."**
- If a detail is inferred rather than stated, label it exactly: **"Inference (low confidence)"**.
- Enterprise-grade quality. **Zero invented endpoints, fields, or behavior.**
- Code must compile as written, follow Java naming conventions, and pass `mvn clean test` against the live /ping endpoint.

---

### O — Output

**Format: Project file tree + file contents only.** No preamble, no explanation, no text outside the artifact.

Produce the artifact in this exact order:

1. A **directory tree** of the full project (tree style).
2. Each file as a separate fenced code block, headed by its relative path:

```
=== <relative/path/to/File.java> ===
<file contents>
```

#### Mandatory Files (at minimum)

- `pom.xml`
- `testng.xml`
- `src/main/resources/config.properties`  *(base.uri=https://restful-booker.herokuapp.com)*
- `src/main/resources/log4j2.xml`
- `src/main/java/<base>/config/ConfigManager.java`
- `src/main/java/<base>/client/RestClient.java`
- `src/main/java/<base>/client/SpecBuilder.java`
- `src/main/java/<base>/services/HealthCheckService.java`
- `src/main/java/<base>/utils/JsonUtils.java`
- `src/test/java/<base>/tests/BaseTest.java`
- `src/test/java/<base>/tests/PingTests.java`
- `src/test/resources/testdata/ping.json`
- `README.md`

---

### T — Tone

Technical, precise, and enterprise-grade. Output only the requested artifact — no commentary.

---

## Notes for Students

- **Order matters.** R and C set up *who* and *why*; I and P set the guardrails; O and T lock the format.
- **The anti-hallucination block** (the "Don't" rules + the "Insufficient information" fallback) is what makes the output trustworthy for real QA work. Don't skip it.
- **Always attach the actual API spec** — the prompt is only as good as its inputs.
