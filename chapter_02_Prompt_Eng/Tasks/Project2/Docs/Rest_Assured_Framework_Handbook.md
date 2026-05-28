# REST Assured Framework Handbook
*Directory Tree • REST Assured Framework • Restful Booker*

---

## Project Layout

The framework follows a layered Maven structure: configuration, HTTP client/spec builders, service wrappers per resource, DTOs, and tests separated from production code.

```
restful-booker-api-automation/
├── pom.xml
├── testng.xml
├── README.md
├── reports/                          ← Allure HTML output (git-ignore this)
└── src/
    ├── main/
    │   ├── java/com/restfulbooker/automation/
    │   │   ├── config/
    │   │   │   └── ConfigManager.java
    │   │   ├── client/
    │   │   │   ├── SpecBuilder.java
    │   │   │   └── RestClient.java
    │   │   ├── services/
    │   │   │   └── HealthCheckService.java
    │   │   ├── models/
    │   │   │   ├── AuthRequestDto.java
    │   │   │   ├── AuthResponseDto.java
    │   │   │   ├── BookingDto.java
    │   │   │   └── BookingDates.java
    │   │   └── utils/
    │   │       └── JsonUtils.java
    │   └── resources/
    │       ├── config.properties
    │       └── log4j2.xml
    └── test/
        ├── java/com/restfulbooker/automation/tests/
        │   ├── BaseTest.java
        │   └── PingTests.java
        └── resources/
            ├── schemas/
            │   └── booking-response-schema.json
            └── testdata/
                └── ping.json
```

---

## Key Design Decisions

Every decision below is anchored to the Restful Booker spec or to a constraint the framework must satisfy. No invented behaviour.

| Decision | Rationale |
|---|---|
| **SpecBuilder is the only place specs are built** | Tests and services can't drift out of sync on base URI or filters. |
| **RestClient wraps every HTTP verb** | The Allure filter is attached in the spec, not per-call — one capture per request. |
| **ConfigManager resolution order: env var → JVM prop → file** | CI pipelines set BASE_URI; dev overrides via `-D`; file is the safe default. |
| **Invalid-method tests assert ≥ 400, not == 404** | The Restful Booker spec doesn't document error codes for undocumented methods — asserting 404 would be inventing behaviour. |
| **[Inference (low confidence)] tag on 5 tests** | HEAD, POST/PUT/DELETE/PATCH on /ping, and malformed-Accept are not in the spec; labelled so engineers know exactly what to re-verify on a spec update. |
| **BookingDto / AuthRequestDto / AuthResponseDto included now** | All fields are spec-documented — adding them now means zero refactoring when booking and auth tests are added. |

---

## Run It

Standard Maven lifecycle. Smoke gating uses a TestNG group; reports render via the Allure Maven plugin.

| Purpose | Command |
|---|---|
| Full regression | `mvn clean test` |
| Smoke gate (TC-API-001 only) | `mvn clean test -Dgroups=smoke` |
| Open Allure report | `mvn allure:report && open reports/allure-report/index.html` |

---

## Notes

- The `reports/` directory is generated output — keep it in `.gitignore`.
- DTOs under `models/` cover both the current `/ping` iteration and the upcoming auth / booking endpoints — added once, used everywhere.
- Tests under `src/test/java/.../tests/` extend `BaseTest`, which centralises `@BeforeSuite` setup so individual classes stay focused on assertions.
