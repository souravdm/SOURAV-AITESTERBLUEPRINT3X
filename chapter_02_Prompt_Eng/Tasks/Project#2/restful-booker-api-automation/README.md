# Restful Booker API Automation Framework

Enterprise-grade REST Assured framework for the [Restful Booker API](https://restful-booker.herokuapp.com/apidoc/index.html).

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Configuration](#configuration)
4. [Running Tests](#running-tests)
5. [Allure Report](#allure-report)
6. [Framework Architecture](#framework-architecture)
7. [Test Coverage — GET /ping](#test-coverage--get-ping)
8. [Extending the Framework](#extending-the-framework)
9. [CI/CD Integration](#cicd-integration)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Allure CLI *(optional, for local reports)* | 2.27+ |

---

## Project Structure

```
restful-booker-api-automation/
├── pom.xml                          # Maven build — all versions pinned
├── testng.xml                       # TestNG master suite
├── README.md
├── reports/                         # Allure HTML report output (git-ignored)
└── src/
    ├── main/
    │   ├── java/com/restfulbooker/automation/
    │   │   ├── config/
    │   │   │   └── ConfigManager.java        # Singleton config; env-var override
    │   │   ├── client/
    │   │   │   ├── SpecBuilder.java          # RequestSpec / ResponseSpec factories
    │   │   │   └── RestClient.java           # HTTP-verb facade over REST Assured
    │   │   ├── services/
    │   │   │   └── HealthCheckService.java   # /ping endpoint wrapper
    │   │   ├── models/
    │   │   │   ├── BookingDto.java           # Booking request/response POJO
    │   │   │   ├── BookingDates.java         # Nested dates POJO
    │   │   │   ├── AuthRequestDto.java       # POST /auth request POJO
    │   │   │   └── AuthResponseDto.java      # POST /auth response POJO
    │   │   └── utils/
    │   │       └── JsonUtils.java            # Jackson helpers + classpath loader
    │   └── resources/
    │       ├── config.properties             # Base URI, SLA, credentials
    │       └── log4j2.xml                    # Logging configuration
    └── test/
        ├── java/com/restfulbooker/automation/tests/
        │   ├── BaseTest.java                 # Suite lifecycle + config validation
        │   └── PingTests.java               # 12 test methods for GET /ping
        └── resources/
            ├── schemas/
            │   └── booking-response-schema.json  # JSON Schema for POST /booking
            └── testdata/
                └── ping.json                # Data-driven test values for /ping
```

---

## Configuration

All configuration lives in `src/main/resources/config.properties`.  
**No credentials, URIs, or tokens are hardcoded in test classes.**

### Environment-variable overrides (CI-friendly)

| Property | Env Var | Default |
|----------|---------|---------|
| `base.uri` | `BASE_URI` | `https://restful-booker.herokuapp.com` |
| `response.time.sla.ms` | `RESPONSE_TIME_SLA_MS` | `2000` |
| `auth.username` | `AUTH_USERNAME` | `admin` |
| `auth.password` | `AUTH_PASSWORD` | `password123` |
| `log.request` | `LOG_REQUEST` | `true` |
| `log.response` | `LOG_RESPONSE` | `true` |

Resolution order: **environment variable → JVM system property → config.properties**

---

## Running Tests

```bash
# Run the full suite
mvn clean test

# Smoke tests only
mvn clean test -Dgroups=smoke

# Regression suite
mvn clean test -Dgroups=regression

# Performance tests only
mvn clean test -Dgroups=performance

# Negative tests only
mvn clean test -Dgroups=negative

# Override base URI at runtime (e.g., pointing to a local mock)
mvn clean test -Dbase.uri=http://localhost:3001

# Override SLA (e.g., tighter 500 ms during performance profiling)
mvn clean test -Dresponse.time.sla.ms=500
```

---

## Allure Report

```bash
# Generate HTML report after mvn test
mvn allure:report

# Open the report in a browser (requires Allure CLI)
allure serve target/allure-results
```

The generated report is written to `reports/allure-report/`.

---

## Framework Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Test Classes                        │
│  PingTests  →  BaseTest  →  ConfigManager               │
└───────────────────────┬─────────────────────────────────┘
                        │ calls
┌───────────────────────▼─────────────────────────────────┐
│                  Service Layer                           │
│  HealthCheckService  (one class per endpoint group)      │
└───────────────────────┬─────────────────────────────────┘
                        │ delegates to
┌───────────────────────▼─────────────────────────────────┐
│                  Client Layer                            │
│  RestClient   ←──  SpecBuilder  ←──  ConfigManager      │
└───────────────────────┬─────────────────────────────────┘
                        │ sends HTTP via
┌───────────────────────▼─────────────────────────────────┐
│               REST Assured 5.4.0                         │
│  + AllureRestAssured filter  +  Log4j2 logging           │
└─────────────────────────────────────────────────────────┘
```

### Design principles

| Principle | Implementation |
|-----------|---------------|
| No hardcoded config | `ConfigManager` + env-var override |
| Single spec authority | `SpecBuilder` — specs never built in tests |
| HTTP isolation | `RestClient` — tests never call `RestAssured.given()` directly |
| Service Object pattern | One `*Service` class per endpoint group |
| Traceability | Every test carries `// Requirement: REQ-xx | Endpoint: METHOD /path` |
| No invented behaviour | Inferred tests labelled *Inference (low confidence)* |

---

## Test Coverage — GET /ping

| Test ID | Requirement | Scenario | Groups |
|---------|------------|----------|--------|
| TC-API-001 | REQ-HC-001 | 201 status code | smoke, regression, functional |
| TC-API-002 | REQ-HC-002 | Body equals "Created" | regression, functional |
| TC-API-003 | REQ-HC-003 | Content-Type: text/plain | regression, functional |
| TC-API-004 | REQ-HC-004 | Response time ≤ SLA | performance, regression |
| TC-API-005 | REQ-HC-005 | HEAD → 201 + empty body *[Inference]* | regression, functional |
| TC-API-006 | REQ-HC-006 | POST → 4xx *[Inference]* | regression, negative |
| TC-API-007 | REQ-HC-007 | PUT → 4xx *[Inference]* | regression, negative |
| TC-API-008 | REQ-HC-008 | DELETE → 4xx *[Inference]* | regression, negative |
| TC-API-009 | REQ-HC-009 | PATCH → 4xx *[Inference]* | regression, negative |
| TC-API-010 | REQ-HC-010 | 3× idempotent hits → all 201 | regression, functional |
| TC-API-011 | REQ-HC-011 | Malformed Accept header → 201 *[Inference]* | regression, negative |
| TC-API-012 | REQ-HC-012 | Unreachable host → exception | regression, negative |

> **[Inference]** — behaviour not explicitly documented in the Restful Booker spec.  
> Derived from Express.js defaults. Update if live server responds differently.

---

## Extending the Framework

### Add a new endpoint (e.g., POST /auth)

1. **Service** — create `src/main/java/.../services/AuthService.java`:
   ```java
   public static Response createToken(AuthRequestDto body) {
       return RestClient.post("/auth", body, SpecBuilder.buildJsonRequestSpec());
   }
   ```

2. **Tests** — create `src/test/java/.../tests/AuthTests.java` extending `BaseTest`.

3. **Suite** — un-comment the `<test>` block in `testng.xml`.

4. **Schema** — add `src/test/resources/schemas/auth-response-schema.json` and use:
   ```java
   response.then().body(matchesJsonSchemaInClasspath("schemas/auth-response-schema.json"));
   ```

No changes to `SpecBuilder`, `RestClient`, or `ConfigManager` required.

---

## CI/CD Integration

### GitHub Actions

```yaml
- name: Run API Tests
  run: mvn clean test
  env:
    BASE_URI: ${{ secrets.BASE_URI }}
    AUTH_USERNAME: ${{ secrets.AUTH_USERNAME }}
    AUTH_PASSWORD: ${{ secrets.AUTH_PASSWORD }}

- name: Generate Allure Report
  run: mvn allure:report

- name: Upload Allure Results
  uses: actions/upload-artifact@v4
  with:
    name: allure-results
    path: target/allure-results
```

### Jenkins (pipeline snippet)

```groovy
stage('API Tests') {
    steps {
        withCredentials([
            string(credentialsId: 'auth-password', variable: 'AUTH_PASSWORD')
        ]) {
            sh 'mvn clean test'
        }
    }
    post {
        always {
            allure includeProperties: false,
                   jdk: '',
                   results: [[path: 'target/allure-results']]
        }
    }
}
```
