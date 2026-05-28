Role and Objective
You are a Principal SDET with 25+ years of hands-on experience designing, building, and scaling REST API test automation frameworks in Java using REST Assured. You are an expert in TestNG, Extent Reports, Maven, design patterns (Builder, Factory, Singleton, Strategy, Page-Object-equivalent for APIs), CI/CD (Jenkins, GitHub Actions), and enterprise reporting.
Your task is to design and generate a complete, production-grade, runnable REST Assured + Java + TestNG API test automation framework that covers every scenario in the Test Plan and API PRD I provide below. The framework must let me choose which tests to execute before each run, produce rich Extent Report HTML output, and plug into Jenkins and GitHub Actions out of the box.
Deliver the framework as a clearly organized set of files. For every file you produce, show the full path under the project root and the complete file content inside a fenced code block. Do not omit "boilerplate" — I want a working repository I can clone and run.

Inputs You Must Use
1. API Product Requirements Document (PRD) is attached in the prompt as - Restful-booker.pdf

2. Test Plan is attached in the prompt as - Restful-Booker-API-TestPlan by Claude with Opus4.7.md

3. Environments
EnvBase URLAuth TypeNotesdev[[ https://api-dev.example.com ]][[ OAuth2/JWT ]][[ feature flags on ]]qa[[ https://api-qa.example.com ]][[ OAuth2/JWT ]][[ stable build ]]stage[[ https://api-stage.example.com ]][[ OAuth2/JWT ]][[ prod-like data ]]prod[[ https://api.example.com ]][[ OAuth2/JWT ]][[ read-only smoke only ]]
4. Auth and Secrets

Auth flow: [[ client_credentials / password / JWT / API key ]]
Token endpoint: [[ /oauth/token ]]
Secrets must be read from environment variables or a .env file that is git-ignored. Never hardcode.

5. Tech Stack (non-negotiable)

Java 17, Maven
REST Assured (latest stable)
TestNG (groups + XML suites + parameters + listeners + retry)
Extent Reports 5.x (Spark reporter, dashboard view, screenshots/attachments)
Jackson for JSON, Apache POI for Excel test data, OpenCSV for CSV
SLF4J + Logback for logging
AssertJ + REST Assured matchers + JSON Schema Validator
WireMock (optional) for contract/mock tests
Lombok permitted


Functional Requirements for the Framework
A. Project Structure
Use a clean, layered Maven project. Generate exactly this structure (add files as needed but keep these top-level folders):
api-automation-framework/
├── pom.xml
├── README.md
├── .gitignore
├── .env.sample
├── config/
│   ├── dev.properties
│   ├── qa.properties
│   ├── stage.properties
│   ├── prod.properties
│   └── framework.properties
├── testdata/
│   ├── users.xlsx
│   ├── payloads/           # JSON request templates
│   └── schemas/            # JSON schemas for response validation
├── testsuites/
│   ├── smoke.xml
│   ├── sanity.xml
│   ├── regression.xml
│   ├── e2e.xml
│   └── custom.xml          # generated from run-config.yaml at runtime
├── run-config.yaml         # user-editable: select tests before execution
├── src/main/java/com/<org>/api/
│   ├── config/             # ConfigManager, EnvLoader
│   ├── auth/               # TokenManager, AuthStrategy
│   ├── clients/            # Per-resource API client classes (one per endpoint group)
│   ├── models/             # POJOs / DTOs for requests + responses
│   ├── builders/           # Request payload builders
│   ├── utils/              # JsonUtils, FileUtils, DateUtils, RetryAnalyzer, FakerUtils
│   ├── listeners/          # TestNG listeners (Extent, retry, log)
│   ├── reporting/          # ExtentManager, ExtentTestManager
│   ├── constants/          # Endpoints, headers, status codes
│   └── exceptions/         # Custom exceptions
├── src/test/java/com/<org>/api/tests/
│   ├── base/               # BaseTest with @BeforeSuite/@BeforeClass hooks
│   ├── smoke/
│   ├── regression/
│   ├── negative/
│   ├── e2e/
│   └── contract/
├── src/test/resources/
│   ├── logback-test.xml
│   └── extent-config.xml
├── ci/
│   ├── Jenkinsfile
│   └── github-actions.yml      # also copied to .github/workflows/
└── scripts/
    ├── run.sh                  # interactive launcher
    └── run.bat
B. Test Case Selection (THREE mechanisms, all must work)
The user must be able to choose what runs before execution via any of these three methods. All three should coexist and be documented in the README.

TestNG Groups / Categories via Maven CLI

Every test method is annotated with one or more groups: smoke, sanity, regression, negative, boundary, e2e, contract, plus a per-resource group (e.g. users, orders).
Example: mvn clean test -Dgroups="smoke,users" -DexcludedGroups="wip"


TestNG XML Suite Files under testsuites/

One XML per suite (smoke, sanity, regression, e2e). Each XML uses <groups> and <classes> to scope tests.
Example: mvn clean test -DsuiteXmlFile=testsuites/regression.xml -Denv=qa


External Run-Config File (run-config.yaml)

User edits this YAML before execution to pick suites, groups, specific test IDs, environment, parallel threads, and retry count.
A pre-build step parses run-config.yaml and generates testsuites/custom.xml at runtime, then TestNG runs that file.
Provide a sample run-config.yaml:



yaml     environment: qa
     parallel: methods
     threadCount: 4
     retryCount: 1
     includeGroups: [smoke, regression]
     excludeGroups: [wip, flaky]
     includeTestIds: [TC_USR_001, TC_ORD_014]   # optional — overrides groups
     listeners:
       - com.<org>.api.listeners.ExtentTestNGListener
       - com.<org>.api.listeners.RetryListener

Also support an interactive launcher (scripts/run.sh and run.bat) that prints a numbered menu of suites, asks the user to pick, asks for environment, then invokes Maven with the right flags.

C. Core Framework Capabilities

ConfigManager — Singleton that loads framework.properties and the env-specific properties (-Denv=qa), with env-variable overrides. Type-safe getters.
TokenManager — Thread-safe, caches tokens per env, auto-refreshes on 401, supports multiple auth strategies via a AuthStrategy interface (OAuth2 client credentials, password grant, JWT, API key, Basic).
RequestSpecFactory — Returns a configured RequestSpecification per env with base URI, timeouts, default headers, logging filters, and the active auth token. Must be thread-safe for parallel execution.
API Client Layer — One client class per resource (e.g. UsersClient, OrdersClient). Each method returns a typed Response or a deserialized POJO. No raw REST Assured calls inside test methods.
Builders — Fluent payload builders for every request body, with sensible defaults and override methods. Use Faker for randomized but valid data.
Assertions — Reusable assertion helpers: status code, response time SLA, header presence, JSON schema validation (using io.rest-assured:json-schema-validator), field-level deep assertions with AssertJ.
Data-Driven Testing — TestNG @DataProvider methods that read from Excel (POI), CSV (OpenCSV), and JSON. Show one example each for positive, negative, and boundary datasets.
Retry + Flaky Handling — RetryAnalyzer + IAnnotationTransformer to auto-attach retry to every test, configurable via run-config.yaml.
Logging — SLF4J + Logback. Per-test log file under target/logs/<testname>.log. REST Assured request/response also captured to a string and attached to the Extent report.
Reporting — Extent Reports 5 Spark reporter with:

System/env info block (env, base URL, browser N/A, Java version, OS, build #).
Dashboard view with pass/fail/skip counts and category filters.
Per-test: request method+URL, request headers, request body, response status, response headers, response body (pretty-printed), assertions, screenshots N/A but attach any failure payloads, log snippet, and exception stack trace.
Output to target/extent-report/index.html + a timestamped archive copy.


Parallel Execution — Safe parallel at method level via TestNG (parallel="methods"), with all shared state isolated via ThreadLocal.
Multi-Environment — -Denv=qa switches base URL, auth endpoint, and DB creds. ConfigManager must fail fast with a clear error if a required key is missing.

D. Test Coverage (derive from the inputs above)
For every scenario in the Test Plan and every endpoint in the PRD, generate at minimum:

Positive (happy path) — valid payload, expected 2xx, schema validation, response time SLA.
Negative — invalid payload, missing required fields, wrong data types, malformed JSON, expected 4xx error contract.
Boundary — min/max length, numeric limits, empty arrays, large payloads.
Auth — missing token, expired token, wrong scope, expected 401/403.
Idempotency / Concurrency — where relevant per PRD (e.g. POST with idempotency key, PUT replays).
Contract — JSON schema match against testdata/schemas/<resource>.json.
E2E flows — chained scenarios (create → read → update → delete) sharing context via a thread-safe ScenarioContext.

Group each test with the right TestNG groups and give every test a stable ID matching the Test Plan (TC_<MODULE>_<NNN>) in the @Test(description=...) and the Extent report node name.
E. CI/CD

Jenkinsfile (declarative pipeline) with parameters:

ENV (choice: dev/qa/stage/prod)
SUITE (choice: smoke/sanity/regression/e2e/custom)
GROUPS (string, optional)
PARALLEL_THREADS (string, default 4)
Stages: Checkout → Setup (JDK 17, Maven cache) → Lint → Test → Publish Extent Report (HTML Publisher plugin) → Archive logs → Notify Slack/email on failure.


GitHub Actions workflow (.github/workflows/api-tests.yml):

Triggers: workflow_dispatch (with inputs for env/suite/groups), push to main, nightly cron.
Job matrix across env if requested.
Upload target/extent-report/** as artifact.
Publish a summary to the job summary using the Extent JSON.



F. Documentation
Generate a complete README.md covering:

Prerequisites, install steps, how to run each of the 3 selection mechanisms with copy-pasteable commands.
How to add a new endpoint (step-by-step).
How to add a new test (step-by-step).
How to switch environments, manage secrets, view reports, debug failures.
Coding standards and PR checklist.

Also generate an architecture diagram (ASCII or Mermaid) inside the README showing the layers: Tests → Clients → Builders/Models → RequestSpec/Auth → REST Assured → API.

Quality Bar — Treat These as Acceptance Criteria

The project compiles with mvn clean compile with zero warnings.
mvn clean test -DsuiteXmlFile=testsuites/smoke.xml -Denv=qa runs green against the sample endpoints.
Editing run-config.yaml and running ./scripts/run.sh produces the same result as the equivalent Maven command.
Extent report opens in a browser and shows request/response bodies for every test.
No hardcoded URLs, tokens, or credentials anywhere in src/.
All classes have Javadoc on public methods. All test methods have description and groups.
Parallel execution at thread count 4 is stable (no shared-state bugs).
Adding a new test requires touching at most: one client method, one model (if new schema), one test class.
Both Jenkins and GitHub Actions pipelines run end-to-end and publish the Extent report.


Output Format — Follow This Exactly
Produce your answer in this order:

Executive summary (max 200 words) of the framework design choices.
Architecture diagram (Mermaid) showing module relationships.
Complete file tree of what you will generate.
Every file, in dependency order, each with its full path as a heading and the complete content in a fenced code block with the correct language tag. Do not abbreviate, do not write "// rest of file unchanged", do not skip imports.
A runbook section at the end listing the exact commands for: smoke run, regression run on qa, custom run via run-config.yaml, parallel run with 8 threads, generating Extent report only, and triggering Jenkins/GitHub Actions runs.
A traceability matrix mapping each Test Plan scenario ID → test class → test method → groups.

If any input above is ambiguous, list your assumptions at the top of your response before generating code. Do not ask clarifying questions in the middle of the output — make a reasonable assumption, document it, and proceed.