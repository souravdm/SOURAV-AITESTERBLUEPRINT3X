**TEST PLAN**

**Restful-booker API**

_Playground API – Auth, Booking, and Ping modules_

Prepared by: QA / Test Engineering

Document Version: 1.0

Date: 25 May 2026

# **1\. Document Control**

| **Field** | **Value** |
| --- | --- |
| Document Name | Test Plan – Restful-booker API |
| Version | 1.0 |
| Prepared By | QA Lead / Test Engineering Team |
| Reviewed By | QA Manager, Backend Engineering Lead |
| Approved By | Product Owner / Engineering Manager |
| Date | 25 May 2026 |
| Source Document | Restful-booker Product Requirement Document (PRD) – generated 2025-06-11 via apidoc 0.25.0 |
| Base URL Under Test | https://restful-booker.herokuapp.com |

### **Revision History**

| **Version** | **Date** | **Author** | **Reviewer** | **Summary of Changes** |
| --- | --- | --- | --- | --- |
| 0.1 | 20 May 2026 | QA Engineer | QA Lead | Initial draft based on PRD walkthrough. |
| 0.2 | 22 May 2026 | QA Lead | Backend Lead | Added negative scenarios, schema validation, and assumptions. |
| 1.0 | 25 May 2026 | QA Lead | Engineering Manager | Baselined for execution. Open questions captured for clarification. |

# **2\. Objective**

The purpose of this Test Plan is to define the strategy, scope, approach, deliverables, and execution criteria for validating the Restful-booker REST API as described in the uploaded PRD. The application under test is a playground booking API that exposes Auth, Booking (CRUD + partial update + search), and Ping (health) endpoints over HTTP.

The testing effort is designed to reduce the following quality risks:

- Functional correctness of CRUD operations on bookings (Create, Read, Update, Partial Update, Delete).
- Authentication and authorization correctness for state-changing endpoints (PUT, PATCH, DELETE) using both Cookie token and Basic auth.
- Content-negotiation correctness across application/json, application/xml, text/xml, and application/x-www-form-urlencoded payloads and responses.
- Data validation correctness for guest names, prices, deposit flag, check-in/check-out dates, and additional needs.
- Search/filter correctness on the GET /booking endpoint (by firstname, lastname, checkin, checkout).
- Stability of the public health endpoint (/ping) which gates downstream smoke pipelines.
- Backward-compatibility of the documented response schemas across releases.

The plan explicitly verifies the eight endpoints documented in the PRD and the supporting header, parameter, and payload contracts. Items not present in the PRD are listed under Section 4.2 (Out of Scope), Section 19 (Assumptions), or Section 21 (Open Questions).

# **3\. Product / Requirement Summary**

Restful-booker is a REST API used as a playground for API testing practice. The PRD documents three functional modules:

- Auth – issues a session token used to authorize state-changing booking operations.
- Booking – the core CRUD module managing booking records (guest details, price, deposit flag, check-in/check-out dates, additional needs).
- Ping – a health check endpoint.

### **Key user / system workflows**

- Token acquisition: client POSTs admin credentials to /auth and receives a token.
- Booking lifecycle: client creates a booking, retrieves it by ID, lists/searches IDs, updates it (full or partial), and deletes it.
- Search workflow: client filters booking IDs by firstname, lastname, checkin, or checkout.
- Health workflow: external monitor calls /ping to confirm service availability.

### **Supported request and response formats (per PRD)**

- Request Content-Type: application/json (default), text/xml, application/x-www-form-urlencoded.
- Response Accept: application/json (default), application/xml.
- Authentication for PUT, PATCH, DELETE /booking/:id: Cookie header (token=&lt;token&gt;) OR Basic Authorization header.

### **Roles / actors**

- Unauthenticated client – may call GET /booking, GET /booking/:id, POST /booking, GET /ping, and POST /auth.
- Authenticated client – additionally may call PUT, PATCH, and DELETE on /booking/:id.

### **Integration points**

- None are explicitly called out in the PRD beyond the API surface itself. See Section 21 (Open Questions) for clarification needs around persistence layer, rate limiting, and observability.

# **4\. Scope of Testing**

## **4.1 In Scope**

The following endpoints, headers, parameters, and behaviors documented in the PRD are in scope for functional, negative, boundary, schema, and authorization testing.

| **Endpoint** | **Method** | **Auth** | **Purpose** | **Key Inputs** | **Expected Response** |
| --- | --- | --- | --- | --- | --- |
| /auth | POST | None | Create auth token | Body: username, password. Header: Content-Type: application/json. | 200 with { token: "&lt;string&gt;" } when credentials match admin/password123 defaults. |
| /booking | GET | None | List booking IDs (optional filters) | Query: firstname, lastname, checkin (CCYY-MM-DD), checkout (CCYY-MM-DD) – all optional. | 200 with array of { bookingid: &lt;number&gt; }. |
| /booking/:id | GET | None | Retrieve a single booking | URL: id. Header: Accept = application/json \| application/xml. | 200 with booking object (firstname, lastname, totalprice, depositpaid, bookingdates{checkin, checkout}, additionalneeds) in requested format. |
| /booking | POST | None | Create a new booking | Headers: Content-Type (json\|xml\|x-www-form-urlencoded), Accept (json\|xml). Body: full booking object. | 200 with { bookingid, booking{...} } in requested format. |
| /booking/:id | PUT | Required (Cookie token OR Basic auth) | Full update of an existing booking | URL: id. Body: full booking object. Headers: Content-Type, Accept, Cookie or Authorization. | 200 with the updated booking object in requested format. |
| /booking/:id | PATCH | Required (Cookie token OR Basic auth) | Partial update of an existing booking | URL: id. Body: any subset of booking fields. Headers as above. | 200 with the updated booking object in requested format. |
| /booking/:id | DELETE | Required (Cookie token OR Basic auth) | Delete a booking | URL: id. Headers: Cookie or Authorization. | 201 Created per PRD response (PRD labels success row as Success 200 but example shows 201 – see Open Questions). |
| /ping | GET | None | Health check | None. | 201 Created per PRD response (PRD labels success row as Success 200 but example shows 201 – see Open Questions). |

**In scope test activities:**

- Functional verification of every endpoint above.
- Positive, negative, boundary, and equivalence-class data testing.
- Authorization enforcement on PUT, PATCH, and DELETE /booking/:id (Cookie token and Basic auth paths).
- Schema validation (JSON and XML) against PRD-documented response fields.
- Content-negotiation: Content-Type vs. Accept matrix for POST, PUT, PATCH on /booking and GET /booking/:id.
- Query-string filter behavior on GET /booking.
- Idempotency check of PUT and DELETE (resending the same request).
- Regression of all documented endpoints after each release.
- Smoke testing using /ping and /auth.

## **4.2 Out of Scope**

The following items are out of scope for this test cycle because they are not described in the PRD or are not part of the API surface being delivered:

- UI, browser, or mobile testing (the PRD describes only an HTTP API).
- Performance, load, stress, and soak testing (no NFRs specified in PRD – see Section 17).
- Penetration testing and threat modeling beyond the basic auth checks listed in Section 6.
- Database-level testing (schema, indexes, replication) – the persistence layer is not documented.
- Localization / internationalization testing (no locale requirements in PRD).
- Backup, restore, and disaster recovery testing.
- Third-party integration testing (no integrations documented).
- Rate-limiting and throttling validation (not specified).
- Long-term token expiry validation beyond what the PRD describes (PRD does not state token TTL).

# **5\. Test Items / Features to be Tested**

| **Req ID** | **Feature / Endpoint** | **Description** | **Coverage Required** | **Priority** | **Test Type** |
| --- | --- | --- | --- | --- | --- |
| REQ-AUTH-01 | POST /auth | Create auth token with username and password. | Positive (valid creds), Negative (invalid creds, missing fields, wrong content-type), Schema validation. | P1  | Functional, Security, Schema |
| REQ-BOOK-01 | GET /booking | Return all booking IDs. | Positive (no filter returns array), Response schema, empty-DB behavior. | P1  | Functional, Schema |
| REQ-BOOK-02 | GET /booking (filters) | Return booking IDs filtered by firstname / lastname / checkin / checkout. | Positive, Negative (unknown name, malformed date), Boundary (date equal to record), Combined filters, Case sensitivity. | P1  | Functional, Boundary, Negative |
| REQ-BOOK-03 | GET /booking/:id | Retrieve a booking by ID; supports JSON and XML response. | Positive (existing ID, JSON), Positive (existing ID, XML), Negative (non-existent ID, non-numeric ID), Header negotiation. | P1  | Functional, Schema, Negative |
| REQ-BOOK-04 | POST /booking | Create a booking with JSON, XML, or form-urlencoded body; respond in JSON or XML. | Positive matrix of Content-Type x Accept, Negative (missing field, wrong types, mismatched content-type, malformed body), Schema validation, Boundary on dates and totalprice. | P1  | Functional, Negative, Boundary, Schema |
| REQ-BOOK-05 | PUT /booking/:id | Full update of an existing booking; requires Cookie token or Basic auth. | Positive (Cookie auth), Positive (Basic auth), Negative (no auth, invalid token, wrong Basic creds), Schema validation, Idempotency. | P1  | Functional, Security, Negative, Schema |
| REQ-BOOK-06 | PATCH /booking/:id | Partial update of an existing booking; requires Cookie token or Basic auth. | Positive (single field), Positive (multiple fields), Positive (nested bookingdates only), Negative (unknown field, no auth), Schema validation. | P1  | Functional, Security, Negative, Schema |
| REQ-BOOK-07 | DELETE /booking/:id | Delete an existing booking; requires Cookie token or Basic auth. | Positive (Cookie auth), Positive (Basic auth), Negative (no auth, wrong token, already-deleted ID, non-existent ID), Status code verification. | P1  | Functional, Security, Negative |
| REQ-PING-01 | GET /ping | Health-check endpoint. | Positive (returns 201 per PRD example), Response time sanity, Status code verification. | P2  | Smoke, Functional |
| REQ-CROSS-01 | Cross-cutting: Auth on protected routes | Verify Cookie and Basic auth equivalency on PUT/PATCH/DELETE. | Positive matrix, Negative (expired/forged token, malformed Basic header, missing both headers). | P1  | Security, Functional |
| REQ-CROSS-02 | Cross-cutting: Content-negotiation | Validate Content-Type and Accept handling across all write endpoints. | Matrix coverage, Negative (unsupported MIME). | P2  | Functional, Negative |
| REQ-CROSS-03 | Cross-cutting: Schema contract | Validate documented response schema fields, types, and structure. | JSON schema and XML schema assertions on every endpoint. | P1  | Schema / Contract |

# **6\. Test Approach / Test Strategy**

Testing will be executed in layered passes (smoke -> functional -> negative -> integration -> regression) and will be predominantly black-box API testing. Each pass is described below with the rationale and the areas it will cover. Only test types relevant to the PRD are included.

### **Smoke testing**

Why: confirm the API is reachable and the authentication path is working before any deeper testing begins.

What: GET /ping, POST /auth with valid credentials, GET /booking. Expected outcomes: /ping returns documented status, /auth returns a token, /booking returns an array.

### **Functional testing**

Why: confirm each endpoint behaves as documented in the PRD.

What: every endpoint listed in Section 4.1 against the documented inputs and outputs. Includes JSON, XML, and form-urlencoded variants on POST /booking and PUT /booking/:id.

### **API testing**

Why: the application is exclusively an HTTP API; verification will assert status codes, headers, body, and response time at the HTTP layer.

What: status code assertions (200, 201, 4xx where applicable), Content-Type response header, response body field presence and types, and timing measurements per request.

### **Positive testing**

Why: confirm documented happy paths produce documented outputs.

What: valid payloads for all CRUD operations, valid tokens for protected routes, valid query strings on GET /booking.

### **Negative testing**

Why: confirm the API rejects malformed or unauthorized input in a safe and predictable manner.

What: missing required fields, type mismatches (e.g., totalprice as string), invalid date formats, non-existent IDs, missing or invalid auth, payload not matching declared Content-Type, unsupported MIME types.

### **Boundary value testing**

Why: defects often cluster at edges (empty strings, zero, very large numbers, oldest/newest dates).

What: totalprice = 0, 1, 999999999; firstname/lastname empty, 1 char, 255 char, multi-byte; checkin = checkout; checkin = 0001-01-01; checkout = 9999-12-31.

### **Data validation testing**

Why: PRD declares field types (String, Number, Boolean, Date) but does not document validation rules; testing will probe the API’s behavior and feed gaps back to product.

What: type coercion of depositpaid (true/false vs. 1/0 vs. 'true'), date format CCYY-MM-DD, numeric vs. string totalprice, null/blank handling, special characters in additionalneeds.

### **Authentication and authorization testing**

Why: PUT, PATCH, and DELETE /booking/:id require auth; weak enforcement here is high-impact.

What: valid Cookie token, valid Basic auth, expired/forged token, missing both headers, Basic auth with wrong credentials, header injection attempts.

### **Error handling testing**

Why: PRD does not specify error response shapes; capturing actual error behavior is necessary for contract publication.

What: trigger 4xx scenarios deliberately and document status code, body, and headers for each. Flag any inconsistencies.

### **Integration testing**

Why: although the PRD documents endpoints independently, real workflows chain them: create -> get -> update -> delete using a fresh /auth token.

What: end-to-end flows that use one endpoint’s output as another’s input.

### **Regression testing**

Why: protect against unintended side-effects of fixes and new releases.

What: a curated subset of P1 functional and authorization cases, automated and run on every release branch.

### **Retesting**

Why: confirm defect fixes.

What: rerun the exact failing case, then a small set of related cases to confirm no collateral regression.

### **Exploratory testing**

Why: surface defects the documented cases will miss (e.g., concurrent updates, ID collisions, large payloads).

What: time-boxed sessions per module with a charter such as 'try to corrupt a booking via concurrent PUT and PATCH'.

### **Contract / schema validation**

Why: the PRD is the contract; consumers depend on it.

What: assert every response field documented in the PRD is present, correctly typed, and not renamed. Apply JSON Schema for JSON responses and XSD or XPath assertions for XML responses.

### **Performance testing (only if required)**

PRD does not define NFRs. Listed only for completeness; gated on Open Question OQ-06.

### **Security testing (basic)**

Why: protected endpoints accept Basic auth and Cookie tokens.

What: verify token cannot be reused across users, verify Basic auth with wrong credentials is rejected, confirm tokens are not echoed back in logs or error bodies, basic injection probes (SQL, NoSQL, XML External Entity) on writable fields.

# **7\. Test Design Techniques**

| **Technique** | **Where Used** | **Example** |
| --- | --- | --- |
| Equivalence Partitioning | All input fields on POST/PUT/PATCH /booking; query string filters on GET /booking. | Class A: valid firstname (letters), Class B: empty string, Class C: numeric, Class D: special characters / unicode. |
| Boundary Value Analysis | totalprice, checkin, checkout, string-length fields. | totalprice = -1, 0, 1, 999999999. checkin one day before checkout, equal to checkout, after checkout. |
| Decision Table Testing | Auth matrix on PUT/PATCH/DELETE. | Inputs: {Cookie present, Authorization present, both, neither, both invalid}. Outputs: success / 401 / 403 (to be confirmed). |
| State Transition Testing | Booking lifecycle: nonexistent -> created -> updated -> partially updated -> deleted -> nonexistent. | Verify GET behavior at each transition; verify DELETE on a deleted ID. |
| Error Guessing | Authentication and content-negotiation edges. | Send Basic auth with empty password; send Content-Type: text/xml with JSON body; send Accept: text/csv. |
| Pairwise Testing | Content-Type x Accept x Auth-method matrix on PUT /booking/:id. | Reduce 24 combinations to a manageable orthogonal set while keeping coverage of every pair. |
| Exploratory Testing | Cross-cutting risk areas not covered by scripted cases. | Concurrent PUT and DELETE on the same ID; very large additionalneeds payload; rapid token reuse. |

# **8\. Test Scenarios**

High-level scenarios grouped by module. Detailed test cases (step-by-step) will be authored in the test management tool against these scenario IDs.

### **8.1 Auth – POST /auth**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-AUTH-001 | Valid username (admin) and password (password123) returns 200 with non-empty token. | Positive | P1  |
| TS-AUTH-002 | Invalid password returns documented failure response. | Negative | P1  |
| TS-AUTH-003 | Invalid username returns documented failure response. | Negative | P1  |
| TS-AUTH-004 | Missing username field. | Negative | P1  |
| TS-AUTH-005 | Missing password field. | Negative | P1  |
| TS-AUTH-006 | Empty JSON body. | Negative | P2  |
| TS-AUTH-007 | Wrong Content-Type (text/plain) with valid body. | Negative | P2  |
| TS-AUTH-008 | Username with SQL-injection style payload. | Security | P1  |
| TS-AUTH-009 | Response schema contains only the documented 'token' field of type String. | Schema | P1  |
| TS-AUTH-010 | Multiple sequential token requests return distinct or reusable tokens (capture behavior). | Functional | P2  |

### **8.2 Booking – GET /booking**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-GBI-001 | No query string returns 200 with array of { bookingid }. | Positive | P1  |
| TS-GBI-002 | Filter by existing firstname returns matching IDs only. | Positive | P1  |
| TS-GBI-003 | Filter by existing lastname returns matching IDs only. | Positive | P1  |
| TS-GBI-004 | Combined firstname + lastname filter. | Positive | P1  |
| TS-GBI-005 | Filter by checkin >= date returns matching IDs. | Positive / Boundary | P1  |
| TS-GBI-006 | Filter by checkout >= date returns matching IDs. | Positive / Boundary | P1  |
| TS-GBI-007 | Date filter with checkin == checkout (boundary). | Boundary | P2  |
| TS-GBI-008 | Malformed date string (e.g., 13/13/2024) – capture behavior. | Negative | P1  |
| TS-GBI-009 | Filter by non-existent name returns empty array. | Negative | P2  |
| TS-GBI-010 | Case sensitivity of name filter (Sally vs. sally). | Negative | P2  |
| TS-GBI-011 | Unknown query parameter is ignored (or rejected) – capture behavior. | Negative | P3  |
| TS-GBI-012 | Response schema: array of objects, each with numeric bookingid. | Schema | P1  |

### **8.3 Booking – GET /booking/:id**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-GB-001 | Existing ID with Accept: application/json returns documented JSON body. | Positive | P1  |
| TS-GB-002 | Existing ID with Accept: application/xml returns documented XML body. | Positive | P1  |
| TS-GB-003 | Existing ID with no Accept header defaults to JSON. | Positive | P1  |
| TS-GB-004 | Non-existent ID returns documented failure response. | Negative | P1  |
| TS-GB-005 | Non-numeric ID in URL (e.g., /booking/abc). | Negative | P2  |
| TS-GB-006 | Negative ID (/booking/-1). | Negative / Boundary | P2  |
| TS-GB-007 | Very large ID (max safe integer + 1). | Negative / Boundary | P3  |
| TS-GB-008 | Unsupported Accept (e.g., text/csv) – capture behavior. | Negative | P2  |
| TS-GB-009 | Response schema validation for JSON. | Schema | P1  |
| TS-GB-010 | Response schema validation for XML. | Schema | P1  |

### **8.4 Booking – POST /booking**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-CB-001 | Create with JSON body, JSON response – returns 200 and { bookingid, booking{...} }. | Positive | P1  |
| TS-CB-002 | Create with XML body (text/xml), XML response. | Positive | P1  |
| TS-CB-003 | Create with form-urlencoded body. | Positive | P1  |
| TS-CB-004 | Create with JSON body, XML response. | Positive | P2  |
| TS-CB-005 | Create with all fields empty – capture behavior. | Negative | P1  |
| TS-CB-006 | Missing firstname. | Negative | P1  |
| TS-CB-007 | Missing bookingdates. | Negative | P1  |
| TS-CB-008 | totalprice as a string ('111'). | Negative | P1  |
| TS-CB-009 | depositpaid as the string 'yes'. | Negative | P2  |
| TS-CB-010 | checkin after checkout (logical boundary). | Negative / Boundary | P1  |
| TS-CB-011 | checkin == checkout (zero-night booking). | Boundary | P2  |
| TS-CB-012 | Date in wrong format (DD-MM-CCYY). | Negative | P1  |
| TS-CB-013 | firstname with 256+ characters. | Boundary | P2  |
| TS-CB-014 | additionalneeds with Unicode and emoji. | Boundary / Data | P3  |
| TS-CB-015 | Mismatched Content-Type vs. body (Content-Type: text/xml + JSON body). | Negative | P1  |
| TS-CB-016 | Response schema validation for JSON, XML, and URL-encoded variants. | Schema | P1  |

### **8.5 Booking – PUT /booking/:id**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-UB-001 | Valid PUT with Cookie: token=&lt;valid token&gt;. | Positive | P1  |
| TS-UB-002 | Valid PUT with Authorization: Basic &lt;base64&gt;. | Positive | P1  |
| TS-UB-003 | PUT with both Cookie and Authorization headers. | Positive | P2  |
| TS-UB-004 | PUT with no auth header. | Negative / Security | P1  |
| TS-UB-005 | PUT with invalid token. | Negative / Security | P1  |
| TS-UB-006 | PUT with Basic auth using wrong credentials. | Negative / Security | P1  |
| TS-UB-007 | PUT on non-existent ID. | Negative | P1  |
| TS-UB-008 | PUT with missing field in body (e.g., no lastname). | Negative | P1  |
| TS-UB-009 | PUT XML body, XML response. | Positive | P1  |
| TS-UB-010 | PUT URL-encoded body. | Positive | P2  |
| TS-UB-011 | PUT idempotency: same payload twice yields same final state. | Functional | P1  |
| TS-UB-012 | Schema validation of response object. | Schema | P1  |

### **8.6 Booking – PATCH /booking/:id**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-PB-001 | PATCH only firstname. | Positive | P1  |
| TS-PB-002 | PATCH only bookingdates. | Positive | P1  |
| TS-PB-003 | PATCH multiple fields together. | Positive | P1  |
| TS-PB-004 | PATCH with empty JSON body. | Negative | P2  |
| TS-PB-005 | PATCH with unknown field (e.g., 'middlename'). | Negative | P2  |
| TS-PB-006 | PATCH on non-existent ID. | Negative | P1  |
| TS-PB-007 | PATCH without auth. | Negative / Security | P1  |
| TS-PB-008 | PATCH with Cookie token. | Positive / Security | P1  |
| TS-PB-009 | PATCH with Basic auth. | Positive / Security | P1  |
| TS-PB-010 | Response schema validation. | Schema | P1  |
| TS-PB-011 | Confirm HTTP method honored: send actual PATCH (not PUT) – PRD curl examples are inconsistent (see OQ-01). | Functional | P1  |

### **8.7 Booking – DELETE /booking/:id**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-DB-001 | DELETE with Cookie token – status code per PRD example (201) captured. | Positive | P1  |
| TS-DB-002 | DELETE with Basic auth. | Positive | P1  |
| TS-DB-003 | DELETE without auth. | Negative / Security | P1  |
| TS-DB-004 | DELETE with invalid token. | Negative / Security | P1  |
| TS-DB-005 | DELETE on non-existent ID. | Negative | P1  |
| TS-DB-006 | DELETE same ID twice (second call). | State Transition | P1  |
| TS-DB-007 | GET /booking/:id after DELETE confirms removal. | Integration | P1  |
| TS-DB-008 | Capture and verify documented status code (PRD inconsistency – see OQ-02). | Schema / Functional | P1  |

### **8.8 Ping – GET /ping**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-PG-001 | GET /ping returns documented status (201 per example, 200 per label – see OQ-03). | Positive / Smoke | P1  |
| TS-PG-002 | /ping with query string is ignored. | Negative | P3  |
| TS-PG-003 | /ping responds within smoke-test threshold (placeholder). | Functional | P2  |

### **8.9 Cross-cutting**

| **Scenario ID** | **Description** | **Type** | **Priority** |
| --- | --- | --- | --- |
| TS-XC-001 | End-to-end happy path: /auth -> POST /booking -> GET /booking/:id -> PUT -> PATCH -> DELETE -> GET (404). | Integration / Regression | P1  |
| TS-XC-002 | Run full happy path with XML at every step. | Integration | P2  |
| TS-XC-003 | Token reuse across multiple PUTs within same session. | Security | P2  |
| TS-XC-004 | Concurrent PUT and PATCH on same booking ID. | Exploratory | P2  |
| TS-XC-005 | Token leakage check: confirm no token appears in error bodies or echoed back. | Security | P1  |

# **9\. Test Data Strategy**

Test data will be authored from a single fixtures file and parameterized into the test framework. Because Restful-booker is a shared playground, every test run begins with /auth to obtain a fresh token and ends with deletion of any bookings created during the run.

### **Categories of test data**

| **Category** | **Examples** |
| --- | --- |
| Valid data | firstname: 'Jim'; lastname: 'Brown'; totalprice: 111; depositpaid: true; checkin: '2026-06-01'; checkout: '2026-06-05'; additionalneeds: 'Breakfast'. |
| Invalid data | totalprice: '111' (string); depositpaid: 'yes'; checkin: 'tomorrow'; firstname: null. |
| Boundary data | totalprice: 0, 1, 999999999; firstname length: 0, 1, 255, 256; date range: 0001-01-01, 9999-12-31; checkin == checkout. |
| Mandatory-field combinations | All required fields present; each required field omitted in turn. |
| Optional-field combinations | additionalneeds present vs. absent; bookingdates present vs. partial. |
| Duplicate data | Create two bookings with identical body and confirm both get distinct IDs. |
| Non-existent IDs | 0, -1, 999999999, 'abc'. |
| Expired/invalid tokens | 'invalidtoken', '', 'expired123', a token from a previous test run. |
| Special characters | firstname: 'O''Brien', 'Élise', 'Zoë'; additionalneeds: '&lt;script&gt;alert(1)&lt;/script&gt;', emoji 'Breakfast 🥐'. |
| Date formats | Valid: '2026-06-01' (CCYY-MM-DD); Invalid: '01-06-2026', '2026/06/01', '2026-13-01', '2026-02-30'. |
| Large values | additionalneeds with 10 KB string; totalprice = Number.MAX_SAFE_INTEGER. |
| Null / blank values | firstname: ''; depositpaid: null; bookingdates: {}. |

### **Sample request payloads (JSON)**

**Auth – POST /auth**

{ "username": "admin", "password": "password123" }

**Create booking – POST /booking**

{ "firstname": "Jim", "lastname": "Brown", "totalprice": 111, "depositpaid": true, "bookingdates": { "checkin": "2026-06-01", "checkout": "2026-06-05" }, "additionalneeds": "Breakfast" }

**Full update – PUT /booking/:id**

{ "firstname": "James", "lastname": "Brown", "totalprice": 200, "depositpaid": false, "bookingdates": { "checkin": "2026-06-01", "checkout": "2026-06-05" }, "additionalneeds": "Late checkout" }

**Partial update – PATCH /booking/:id**

{ "firstname": "James", "lastname": "Brown" }

# **10\. Environment Requirements**

| **Environment** | **Base URL** | **Build / Version** | **Data Dependency** | **Auth Strategy** | **Tools / Access** |
| --- | --- | --- | --- | --- | --- |
| Public Playground (only environment named in PRD) | https://restful-booker.herokuapp.com | Per PRD generated 2025-06-11 (apidoc 0.25.0). Build/version of the running app is not documented – see OQ-08. | Shared dataset; bookings created by other consumers may appear in GET /booking. Tests must seed and clean their own data. | POST /auth with admin / password123 to obtain a session token. Basic auth as YWRtaW46cGFzc3dvcmQxMjM= (admin:password123). | Postman, Newman, REST Assured, curl. Outbound HTTPS access to the Heroku host. |
| Local / Dev (assumption – not in PRD) | http://localhost:3001 (placeholder) | Locally built from source. | Empty DB at boot; deterministic. | Same admin / password123 credentials assumed. | Docker, Node.js runtime, same test tools as above. |

Note: only the Heroku playground URL is named in the PRD. The local environment row is an assumption and is listed to be confirmed (see OQ-09).

# **11\. Entry Criteria**

- PRD is baselined and version-controlled.
- Open Questions in Section 21 have an owner assigned (clarifications need not all be answered before P2/P3 work begins).
- Test environment(s) are reachable and respond to GET /ping.
- Default admin credentials are valid and a token can be obtained from POST /auth.
- Latest build is deployed to the target environment.
- Smoke pack (TS-PG-001, TS-AUTH-001, TS-GBI-001) has passed.
- Test data fixtures are loaded into the test framework.
- Defect-tracking and test-management tools are accessible.

# **12\. Exit Criteria**

- 100 % of planned P1 test cases executed; 95 % or higher of P2 cases executed.
- Zero open P1 (Critical / Blocker) defects.
- No more than two open P2 (High) defects, each with an approved workaround.
- Regression pack passes on the candidate build.
- All schema/contract tests pass on every documented endpoint.
- Test Summary Report and Defect Report published.
- Sign-off received from QA Lead, Engineering Lead, and Product Owner.

# **13\. Defect Management Process**

Defect reporting tool: Jira (assumption – confirm in OQ-10). All defects use the project key RB-API.

### **Defect lifecycle**

New → Triaged → Assigned → In Progress → Fixed → Ready for Retest → Retesting → Closed (or Reopened).

### **Severity levels**

| **Severity** | **Definition** |
| --- | --- |
| S1 – Critical | Endpoint unusable, data loss, authentication bypass, or full outage. |
| S2 – High | Major functional path broken (e.g., POST /booking returns 5xx for valid data) with no workaround. |
| S3 – Medium | Functional defect with a workaround or affecting non-primary path. |
| S4 – Low | Cosmetic, documentation, or minor schema deviation that does not break consumers. |

### **Priority levels**

| **Priority** | **Definition** |
| --- | --- |
| P1 – Immediate | Fix in the current sprint; blocks release. |
| P2 – High | Fix in next sprint at the latest. |
| P3 – Medium | Fix when capacity allows. |
| P4 – Low | Backlog; revisit at sprint review. |

### **Required defect fields**

- Defect ID, Title, Environment, Build, Steps to reproduce, Expected vs. Actual, Test data, Full request/response (headers + body) for API defects, Logs/screenshots, Severity, Priority, Assignee, Status.

### **Triage process**

Daily triage at 10:00 with QA Lead, Engineering Lead, and Product Owner. New defects are assigned a severity, priority, and owner. Retest within 24 hours of fix delivery.

### **Retest and closure**

Retest the exact failing case, then a small risk-based regression around the fix. Close the defect only when retest passes and regression shows no collateral damage.

### **Defect template**

| **Field** | **Value (example)** |
| --- | --- |
| Defect ID | RB-API-123 |
| Title | PATCH /booking/:id returns 200 but does not persist firstname change |
| Environment | Heroku playground (https://restful-booker.herokuapp.com) |
| Build | 2026.05.25-01 |
| Steps to reproduce | 1) POST /auth to obtain token. 2) POST /booking with valid payload to obtain ID. 3) PATCH /booking/{id} with { firstname: 'Alex' } and Cookie header. 4) GET /booking/{id}. |
| Expected result | Step 4 returns firstname = 'Alex'. |
| Actual result | Step 4 returns the original firstname. |
| Test data | See attached fixture rb-patch-firstname.json |
| Request / Response | Attached as HAR / Postman export. PATCH response: 200 OK, body shows firstname='Alex'. |
| Logs / Screenshots | Attached console output and response headers. |
| Severity | S2 – High |
| Priority | P1  |
| Assigned To | Backend Engineer |
| Status | New |

# **14\. Test Deliverables**

- This Test Plan document.
- Test Scenario register (Section 8) maintained in the test management tool.
- Detailed test cases (one per scenario) with steps, data, and expected results.
- Test data fixtures (JSON/XML/form-urlencoded) checked into source control.
- Postman collection and environment files for Auth, Booking, and Ping.
- REST Assured / TestNG automation scripts for the regression suite.
- Defect reports and weekly defect status export.
- Test Execution Report per build.
- Regression Report per release.
- Test Closure Report at the end of the cycle, including metrics and lessons learned.

# **15\. Test Execution Plan**

Execution proceeds through six phases. Each phase has a gate that must pass before the next begins.

| **Phase** | **Activity** | **Owner** | **Start Date** | **End Date** | **Dependency** | **Deliverable** |
| --- | --- | --- | --- | --- | --- | --- |
| 1\. Smoke | TS-PG-001, TS-AUTH-001, TS-GBI-001 against new build. | QA Engineer | &lt;TBD&gt; | &lt;TBD&gt; | Build deployed. | Smoke result. |
| 2\. Functional | Execute all P1 positive scenarios across modules. | QA Team | &lt;TBD&gt; | &lt;TBD&gt; | Smoke passed. | Functional execution log. |
| 3\. Negative | Execute all negative and boundary scenarios. | QA Team | &lt;TBD&gt; | &lt;TBD&gt; | Functional passed. | Negative execution log. |
| 4\. Integration | End-to-end flows TS-XC-001/002. | QA Engineer | &lt;TBD&gt; | &lt;TBD&gt; | Functional + negative passed. | Integration report. |
| 5\. Regression | Automated regression pack on candidate build. | Automation Engineer | &lt;TBD&gt; | &lt;TBD&gt; | Critical defects fixed. | Regression report. |
| 6\. Defect Retest & Sign-off | Retest fixed defects; final sign-off. | QA Lead | &lt;TBD&gt; | &lt;TBD&gt; | Regression passed. | Test Closure Report. |

# **16\. Automation Strategy**

All endpoints documented in the PRD are well-suited to automation because they are stateless HTTP interactions with deterministic schemas. The strategy is to automate the regression and smoke layers fully, automate the functional layer for happy paths and key negative paths, and leave exploratory and ad-hoc edge cases manual.

### **What to automate**

- All P1 positive cases for every endpoint.
- Auth matrix on PUT/PATCH/DELETE (Cookie vs. Basic vs. none vs. invalid).
- Schema validation on every endpoint response (JSON Schema and XSD/XPath).
- End-to-end happy path (TS-XC-001).
- Content-Type x Accept matrix on POST and PUT /booking.

### **What to keep manual**

- Exploratory sessions (Section 6).
- Visual inspection of error bodies until OQ-04 (error response shape) is resolved.
- Security probes that require human judgment (TS-XC-005).

### **Recommended toolchain**

| **Layer** | **Tool** |
| --- | --- |
| API collection authoring | Postman; export as collection + environment files. |
| CLI / smoke runner | Newman (Postman CLI) for fast smoke runs in CI. |
| Regression framework | REST Assured with Java 17 + TestNG (JUnit 5 acceptable alternative). |
| Build tool | Maven (or Gradle). |
| Schema validation | json-schema-validator for JSON; XMLUnit / XSD for XML. |
| CI / CD | GitHub Actions or Jenkins; trigger on PR + nightly. |
| Reporting | Allure Report (primary); Extent Reports acceptable. |

### **Test layers**

- Contract layer – schema and status-code assertions on every endpoint.
- Functional layer – per-endpoint positive and negative cases.
- End-to-end layer – chained workflows that mirror real usage.
- Non-functional layer – placeholder until OQ-06 is resolved.

### **Data-driven testing**

Parameterize negative inputs (dates, types, lengths) using TestNG DataProviders or Postman iteration data. Keep the fixtures versioned alongside the test code.

### **CI/CD integration and reporting**

Smoke pack on every PR, full regression nightly against the playground URL, and on-demand runs against local/dev. Allure HTML report is published to the build artifacts and linked in the release notes.

# **17\. Non-Functional Testing Considerations**

The PRD does not declare any non-functional requirements (NFRs). The following items are listed for completeness and are gated on confirmation (see Open Questions OQ-06 and OQ-07). Items marked 'requires NFRs' will not be executed until product specifies thresholds.

| **Area** | **Coverage** | **Status** |
| --- | --- | --- |
| Performance / Load | TPS and latency under target load on POST /booking and GET /booking. | Requires NFRs (OQ-06) |
| Stress | Sustained spike traffic to detect capacity limits. | Requires NFRs (OQ-06) |
| Security – Basic | Auth header enforcement, token reuse, injection probes on writable fields. | In scope (see Section 6) |
| Security – Advanced | Penetration test, dependency scan, OAuth replay attacks. | Out of scope unless requested (OQ-07) |
| Reliability | Uptime and error-rate observations during the test cycle. | Best-effort during execution |
| Availability / Health | Continuous /ping monitoring during regression runs. | In scope |
| Compatibility | HTTP/1.1 and HTTP/2 client compatibility. | Best-effort |
| Observability | Verify error responses contain enough information for client debugging. | In scope (also informs OQ-04) |

# **18\. Risks and Mitigations**

| **Risk ID** | **Description** | **Impact** | **Probability** | **Mitigation** | **Owner** |
| --- | --- | --- | --- | --- | --- |
| R-01 | PRD inconsistencies (PATCH curl uses PUT; DELETE/ping status codes labelled 200 but examples show 201). | Medium | High | Capture as Open Questions OQ-01, OQ-02, OQ-03 and treat actual API behavior as authoritative until product clarifies. | QA Lead |
| R-02 | Public playground is shared and rate-limited by Heroku. | Medium | Medium | Throttle test runs; isolate test data using unique guest names; back-off on 5xx; consider local build for heavy runs. | QA Engineer |
| R-03 | Missing test data – playground may be wiped without notice. | Medium | Medium | Seed data at the start of every test run; never rely on pre-existing IDs. | QA Engineer |
| R-04 | Authentication issues (token expiry, throttling). | High | Medium | Refresh token at the start of every protected request set; fall back to Basic auth. | QA Engineer |
| R-05 | Third-party dependency (Heroku platform outage). | High | Low | Monitor /ping and Heroku status page; reschedule blocked runs. | QA Lead |
| R-06 | Time constraints (release timeline tight). | High | Medium | Prioritize P1 scenarios; gate release on P1 closure; deferred P3 documented and tracked. | QA Lead / PM |
| R-07 | Flaky tests due to network or shared environment. | Medium | Medium | Add retry-on-network-failure (max 2); separate flakiness budget; quarantine flaky tests pending fix. | Automation Engineer |
| R-08 | Undocumented error response specification. | High | High | Capture actual error responses during testing and publish a discovered-contract document; raise OQ-04. | QA Lead |
| R-09 | Insufficient validation rules documented (mandatory fields, types). | Medium | High | Document observed behavior and propose validation rules for product to ratify; raise OQ-05. | QA Lead |
| R-10 | PRD does not document token lifecycle (TTL, revocation). | Medium | Medium | Test token reuse across hours; record observed TTL; raise OQ-11. | QA Engineer |

# **19\. Assumptions**

- The Heroku playground is the system under test and is available throughout the test cycle.
- admin / password123 remain the valid credentials for POST /auth and Basic auth (Basic header YWRtaW46cGFzc3dvcmQxMjM=).
- Tokens issued by POST /auth are valid for a single test run; refresh is acceptable between runs.
- PRD-documented response fields are the contract; any additional fields returned by the API will be flagged but not necessarily failed.
- Date format CCYY-MM-DD applies to both request and response (per PRD).
- Missing fields in PATCH leave the existing values unchanged.
- Bookings are created with auto-generated IDs; clients do not specify IDs on POST.
- Test team has outbound HTTPS connectivity to \*.herokuapp.com.
- Jira is the defect-tracking tool unless OQ-10 indicates otherwise.

# **20\. Dependencies**

- Availability and accuracy of the PRD (uploaded version dated 24/05/2026).
- Stable test environment(s) per Section 10.
- Working POST /auth flow (without it, PUT/PATCH/DELETE cannot be tested).
- Test data fixtures committed to the test repository.
- Valid admin credentials and Basic auth header value.
- Development fixes turned around within the agreed SLA (P1 same-day, P2 within 2 days).
- CI/CD access for QA to push and run pipelines.
- Product owner availability to answer Open Questions in Section 21.

# **21\. Open Questions**

The PRD has several gaps and inconsistencies. These must be confirmed by Product / Engineering before they can be locked into the test pass/fail criteria.

| **ID** | **Question** | **Source / Evidence** | **Owner** | **Status** |
| --- | --- | --- | --- | --- |
| OQ-01 | The PATCH /booking/:id section shows curl examples using -X PUT, not -X PATCH. Should the actual HTTP verb be PATCH (matching the section title) or PUT (matching the curl examples)? | PRD: 'Booking - PartialUpdateBooking' section. | Backend Lead | Open |
| OQ-02 | DELETE /booking/:id Success table is labelled 'Success 200' but the response example is 'HTTP/1.1 201 Created'. Which is correct? | PRD: 'Booking - DeleteBooking' section. | Backend Lead | Open |
| OQ-03 | GET /ping Success table is labelled 'Success 200' but the response example is 'HTTP/1.1 201 Created'. Which is correct? | PRD: 'Ping - HealthCheck' section. | Backend Lead | Open |
| OQ-04 | Error response specification is missing for every endpoint. What status code and body are returned for invalid credentials, missing fields, malformed dates, non-existent IDs, and missing auth? | PRD: no error tables. | Product Owner | Open |
| OQ-05 | Validation rules are not documented (mandatory fields, allowed character sets, min/max length, totalprice range, depositpaid accepted values). Please confirm. | PRD: request body tables list types only. | Product Owner | Open |
| OQ-06 | Are there performance NFRs (TPS, p95 latency, concurrency)? | PRD: no NFR section. | Product Owner | Open |
| OQ-07 | Is a security / penetration test required for this release? | PRD: no security section. | Security / Product | Open |
| OQ-08 | What build/version of the API is deployed to the playground at test time? | PRD does not include a build/version field. | DevOps | Open |
| OQ-09 | Is there a dedicated test environment (other than the public playground) that QA can use without rate limits and shared data? | Only the Heroku URL is named in PRD. | DevOps | Open |
| OQ-10 | What defect-tracking tool should be used (assumed Jira)? | Not specified in PRD. | PM  | Open |
| OQ-11 | What is the lifetime and revocation behavior of the token returned by POST /auth? | PRD does not describe token TTL. | Backend Lead | Open |
| OQ-12 | POST /booking response wraps the booking inside { bookingid, booking{...} } whereas PUT and PATCH return the booking flat. Is this intentional asymmetry? | PRD: 'Booking - CreateBooking' vs. 'Booking - UpdateBooking' Success sections. | Backend Lead | Open |
| OQ-13 | How does GET /booking behave when checkin/checkout filters are combined with name filters – AND or OR semantics? | PRD does not state combination semantics. | Backend Lead | Open |
| OQ-14 | GET /booking checkin/checkout filters are described as 'greater than or equal to' – is this the intended semantics, or should they be a range filter? | PRD: GetBookingIds parameter table. | Product Owner | Open |

# **22\. Approval / Sign-Off**

| **Name** | **Role** | **Approval Status** | **Date** | **Comments** |
| --- | --- | --- | --- | --- |
| &lt;TBD&gt; | QA Lead | Pending | &lt;TBD&gt; |     |
| &lt;TBD&gt; | Engineering Manager | Pending | &lt;TBD&gt; |     |
| &lt;TBD&gt; | Product Owner | Pending | &lt;TBD&gt; |     |
| &lt;TBD&gt; | Backend Tech Lead | Pending | &lt;TBD&gt; |     |
| &lt;TBD&gt; | Release Manager | Pending | &lt;TBD&gt; |     |