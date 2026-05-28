# Restful-Booker API Automation Framework

Production-grade REST Assured + Java 17 + TestNG framework for the [Restful-Booker](https://restful-booker.herokuapp.com) API, generated from the Restful-Booker Test Plan and PRD.

---

## Architecture

```
src/
├── main/java/com/restfulbooker/api/
│   ├── auth/           AuthStrategy, CookieTokenStrategy, BasicAuthStrategy, TokenManager
│   ├── builders/       BookingBuilder (Faker defaults + fluent overrides)
│   ├── clients/        AuthClient, BookingClient, PingClient, RequestSpecFactory
│   ├── config/         ConfigManager (singleton, multi-env, env-var resolution)
│   ├── constants/      Endpoints, HttpHeaders, StatusCodes
│   ├── exceptions/     FrameworkException, ConfigException, AuthException
│   ├── listeners/      ExtentTestNGListener, RetryAnalyzer, RetryListener
│   ├── models/         AuthRequest/Response, Booking, BookingDates, CreateBookingResponse, BookingIdResponse
│   ├── reporting/      ExtentManager, ExtentTestManager
│   └── utils/          AssertionHelper, DateUtils, ExcelDataProvider, FakerUtils, JsonUtils, RunConfigGenerator
└── test/java/com/restfulbooker/api/tests/
    ├── base/           BaseTest, ScenarioContext
    ├── smoke/          PingTests, AuthSmokeTests
    ├── regression/     AuthTests, BookingGetTests, CreateBookingTests, UpdateBookingTests,
    │                   PatchBookingTests, DeleteBookingTests
    ├── negative/       BoundaryTests
    ├── e2e/            E2EBookingFlowTests
    └── contract/       BookingContractTests

testdata/
├── payloads/           JSON fixture files
└── schemas/            JSON Schema Draft-7 files for contract validation

testsuites/
├── smoke.xml           Smoke suite (PingTests + AuthSmokeTests)
├── sanity.xml          Sanity suite (Smoke + Contract)
├── regression.xml      Full regression (Auth + CRUD + Boundary + Contract)
└── e2e.xml             E2E lifecycle flow (single-threaded, ordered)

config/
├── framework.properties  Framework-wide constants (SLA, retry, parallel count)
├── qa.properties         QA environment config (no secrets — env vars expected)
├── stage.properties
└── prod.properties
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 17+ |
| Maven | 3.9+ |
| Git  | any |

---

## Environment Setup

All secrets are read from environment variables. Never store credentials in source files.

```bash
cp .env.sample .env       # fill in your values
source .env               # or load via your CI secret manager
```

| Variable | Description |
|----------|-------------|
| `RB_QA_USERNAME` | API username for QA environment |
| `RB_QA_PASSWORD` | API password for QA environment |
| `RB_QA_BASIC_AUTH` | Pre-encoded Base64 `username:password` for Basic auth header |
| `RB_PROD_*` | Equivalent for production |

---

## Running Tests

### Using the convenience scripts

```bash
# macOS / Linux
./scripts/run.sh [suite] [env] [threads]
./scripts/run.sh regression qa 4

# Windows
scripts\run.bat [suite] [env] [threads]
scripts\run.bat smoke qa 2
```

### Using Maven directly

```bash
# Run specific suite
mvn clean test -DsuiteXmlFile=testsuites/regression.xml -Denv=qa

# Run by group (overrides suite XML group filter)
mvn clean test -Dgroups=smoke -Denv=qa

# Run E2E (single-threaded)
mvn clean test -DsuiteXmlFile=testsuites/e2e.xml -Denv=qa

# Generate custom suite from run-config.yaml
mvn clean test -Prun-config -Denv=qa
```

### Available suites

| Suite | Description | Parallel |
|-------|-------------|---------|
| `smoke.xml` | Fast health-check: ping + auth token | `methods`, 2 threads |
| `sanity.xml` | Smoke + contract schema validation | `methods`, 3 threads |
| `regression.xml` | Full regression: auth, CRUD, boundary, contract | `classes`, 4 threads |
| `e2e.xml` | End-to-end booking lifecycle (create→read→update→patch→delete) | none (ordered) |

---

## Test Report

After each run, the Extent HTML report is written to:

```
target/extent-report/index.html
```

A timestamped archive copy is also saved under `target/extent-report/archive/`.

---

## Test Coverage

| Test Plan ID | Class | Method | Groups |
|-------------|-------|--------|--------|
| TS-PG-001 | PingTests | tc_ping_001_healthCheckReturns201 | smoke, ping |
| TS-PG-003 | PingTests | tc_ping_002_healthCheckResponseTimeWithinSla | smoke, ping |
| TS-AUTH-001 | AuthSmokeTests / AuthTests | tc_auth_001_* | smoke / regression, auth |
| TS-AUTH-002..009 | AuthTests | tc_auth_002..009_* | regression, auth, negative/schema/security |
| TS-GBI-001..012 | BookingGetTests | tc_gbi_001..012_* | regression, booking |
| TS-GB-001..010 | BookingGetTests | tc_gb_001..010_* | regression, booking |
| TS-CB-001..016 | CreateBookingTests | tc_cb_001..016_* | regression, booking |
| TS-UB-001..012 | UpdateBookingTests | tc_ub_001..012_* | regression, booking |
| TS-PB-001..011 | PatchBookingTests | tc_pb_001..011_* | regression, booking |
| TS-DB-001..008 | DeleteBookingTests | tc_db_001..008_* | regression, booking |
| TC_BND_001..013 | BoundaryTests | tc_bnd_001..013_* | regression, boundary, negative |
| TS-XC-001..005 | E2EBookingFlowTests | tc_xc_001..005_* | e2e |
| TC_CT_001..010 | BookingContractTests | tc_ct_001..010_* | contract |

---

## CI/CD

### GitHub Actions

`.github/workflows/api-tests.yml` triggers on:
- Push/PR to `main` or `develop` (when framework files change)
- Nightly schedule (`0 2 * * *`)
- Manual `workflow_dispatch` (choose suite and environment)

Artifacts uploaded: Extent Report (30 days) + Surefire XML (14 days).

### Jenkins

`ci/Jenkinsfile` provides a parameterized pipeline with:
- `ENV` choice (qa / stage / prod)
- `SUITE` choice (regression / smoke / sanity / e2e)
- `THREAD_COUNT` string (default: 4)
- Credentials via Jenkins Credentials Manager
- HTML report published via `publishHTML` step

---

## Configuration Reference

`config/framework.properties` — framework-wide, env-agnostic:

```properties
response.time.sla.ms=5000
retry.count=1
parallel.thread.count=4
report.output.dir=target/extent-report
```

`config/qa.properties` — environment-specific (no secrets):

```properties
base.url=https://restful-booker.herokuapp.com
auth.type=COOKIE_TOKEN          # COOKIE_TOKEN | BASIC_AUTH
```

Secrets are resolved from environment variables at runtime by `ConfigManager`.

---

## Key Design Patterns

| Pattern | Where used |
|---------|-----------|
| Singleton + DCL | ConfigManager, TokenManager, ExtentManager |
| Strategy | AuthStrategy → CookieTokenStrategy / BasicAuthStrategy |
| Builder | BookingBuilder (Faker defaults, fluent overrides) |
| Factory | RequestSpecFactory |
| ThreadLocal | ExtentTestManager, ScenarioContext |
| ReentrantLock | Token refresh in CookieTokenStrategy |
