Test Plan: Restful Booker API

This test plan is based on the attached Restful Booker API documentation. The API includes authentication, booking retrieval, booking creation, full booking update, partial booking update, booking deletion, and health check functionality.  ￼

⸻

1. Document Control

Field	Details
Document Name	Restful Booker API Test Plan
Version	1.0
Prepared By	QA Team
Reviewed By	QA Lead / Test Architect
Approved By	Product Owner / Engineering Lead
Date	TBD
Application	Restful Booker API
Base URL	https://restful-booker.herokuapp.com
API Documentation URL	https://restful-booker.herokuapp.com/apidoc/index.html

Revision History

Version	Date	Author	Description	Reviewed By
1.0	TBD	QA Team	Initial test plan created from API documentation	TBD

⸻

2. Objective

The objective of this test plan is to define the testing scope, strategy, scenarios, data requirements, risks, deliverables, and execution approach for the Restful Booker API.

The testing effort will verify that the API correctly supports:

Capability	Description
Authentication	Generate token using valid credentials
Booking Search	Retrieve all booking IDs and filter booking IDs by name or date
Booking Retrieval	Retrieve booking details by booking ID
Booking Creation	Create new bookings using supported payload formats
Booking Update	Fully update an existing booking
Booking Partial Update	Update selected booking fields only
Booking Deletion	Delete an existing booking using authorization
Health Check	Confirm API availability using ping endpoint

The test plan aims to reduce risks related to incorrect CRUD behavior, broken authentication, invalid response schemas, missing validations, poor error handling, inconsistent content-type handling, and data persistence defects.

⸻

3. Product / Requirement Summary

Restful Booker is a playground booking API that allows users or systems to create, retrieve, update, partially update, and delete hotel-style booking records.

The documentation defines the following API areas:

Module	Description
Auth	Creates an authentication token for protected booking operations
Booking	Provides booking CRUD operations
Ping	Provides a health check endpoint

Key API Workflows

Workflow	Steps
Create and retrieve booking	Create booking → capture bookingid → retrieve booking by ID
Full booking maintenance	Create booking → update booking with complete payload → retrieve and verify updated values
Partial booking maintenance	Create booking → partially update selected fields → retrieve and verify updated and unchanged values
Delete booking	Create booking → authenticate → delete booking → verify booking is no longer available
Availability check	Call /ping and verify API is up

Supported Formats

The documentation mentions support for:

Area	Supported Formats
Request Content-Type	application/json, text/xml, application/x-www-form-urlencoded
Response Accept Header	application/json, application/xml
Authentication	Token via Cookie: token=<token_value> or Basic Authorization header

⸻

4. Scope of Testing

4.1 In Scope

Endpoint	Method	Purpose	Authentication	Parameters / Body	Expected Response
/auth	POST	Create authentication token	Not required	username, password	200 OK, token returned
/booking	GET	Get all booking IDs or filtered IDs	Not required	Optional query params: firstname, lastname, checkin, checkout	200 OK, array of booking IDs
/booking/:id	GET	Get booking by ID	Not required	Path param: id; header: Accept	200 OK, booking details
/booking	POST	Create booking	Not required	Booking payload	200 OK, bookingid and booking object
/booking/:id	PUT	Fully update booking	Required	Path param: id, full booking payload, Cookie or Basic Auth	200 OK, updated booking object
/booking/:id	PATCH	Partially update booking	Required	Path param: id, partial booking payload, Cookie or Basic Auth	200 OK, updated booking object
/booking/:id	DELETE	Delete booking	Required	Path param: id, Cookie or Basic Auth	Success response
/ping	GET	Health check	Not required	None	201 Created

Testing will include:

Area	Coverage
Functional API testing	Verify documented endpoint behavior
Request validation	Validate mandatory, optional, invalid, blank, null, and malformed fields
Response validation	Validate status codes, response body, headers, schema, and data types
Authentication testing	Validate token-based and Basic Auth access for protected endpoints
Authorization negative testing	Verify protected endpoints reject missing or invalid authorization
Data persistence testing	Verify created, updated, partially updated, and deleted records
Contract testing	Validate response structure against expected API contract
Error handling testing	Verify invalid requests return appropriate status codes and safe responses
Regression testing	Verify existing endpoint behavior after changes
Exploratory testing	Identify undocumented behavior or documentation gaps

⸻

4.2 Out of Scope

The following are out of scope unless additional requirements are provided:

Item	Reason
UI testing	No UI requirements are provided
Browser compatibility testing	API-only scope
Mobile device testing	API-only scope
Accessibility testing	No UI exists in the provided requirement
Payment, inventory, or hotel room availability logic	Not documented
User roles and permissions beyond token/basic auth	Not documented
Database-level validation	No database access or schema provided
Production monitoring setup	Not defined in requirement
Backup and recovery testing	Not defined in requirement
Localization/internationalization testing	Not defined in requirement
Formal SLA/load targets	No performance NFRs provided

⸻

5. Test Items / Features to be Tested

Requirement ID	Feature / Endpoint	Description	Test Coverage Required	Priority	Test Type
RB-AUTH-001	POST /auth	Generate auth token	Valid credentials, invalid credentials, missing fields, schema validation	High	Functional, Negative, Security
RB-BOOK-001	GET /booking	Retrieve booking IDs	All IDs, filter by name, filter by dates, invalid filters	High	Functional, Data Validation
RB-BOOK-002	GET /booking/:id	Retrieve booking by ID	Valid ID, invalid ID, non-existent ID, Accept JSON/XML	High	Functional, Negative, Contract
RB-BOOK-003	POST /booking	Create booking	JSON/XML/form payloads, mandatory fields, invalid field types, persistence	High	Functional, Boundary, Contract
RB-BOOK-004	PUT /booking/:id	Full booking update	Token auth, Basic auth, full payload update, invalid auth, invalid ID	High	Functional, Security, Regression
RB-BOOK-005	PATCH /booking/:id	Partial booking update	Partial fields, unchanged fields, invalid auth, invalid data	High	Functional, Data Validation
RB-BOOK-006	DELETE /booking/:id	Delete booking	Token auth, Basic auth, invalid auth, verify deletion	High	Functional, Security
RB-PING-001	GET /ping	Health check	API availability and status code validation	Medium	Smoke, Reliability
RB-E2E-001	End-to-end booking lifecycle	Auth → Create → Get → Update → Patch → Delete → Verify	Full workflow validation	High	Integration, Regression

⸻

6. Test Approach / Test Strategy

Test Type	Why It Is Needed	What Will Be Tested	Example Coverage
Smoke Testing	To confirm the API build is testable	Critical endpoints	/ping, /auth, GET /booking, POST /booking
Functional Testing	To verify documented API behavior	Endpoint request and response behavior	Create, retrieve, update, patch, delete booking
API Testing	Core product is API-based	HTTP method, URL, headers, payload, response	Status codes, headers, body, schema
Positive Testing	To validate expected successful flows	Valid inputs and valid authentication	Create booking with valid JSON
Negative Testing	To verify graceful handling of invalid usage	Invalid payloads, missing auth, invalid IDs	Delete without token
Boundary Value Testing	To identify data handling issues	String length, numeric values, dates	totalprice = 0, very large price, long names
Data Validation Testing	To verify request field validation	Required fields, data types, date formats	Invalid date format, boolean as string
Authentication Testing	PUT, PATCH, DELETE require authorization	Cookie token and Basic Auth	Valid token, invalid token, missing token
Authorization Negative Testing	To prevent unauthorized data modification	Protected endpoints without credentials	PUT/PATCH/DELETE without auth
Error Handling Testing	To validate failure responses	Invalid IDs, malformed payloads, unsupported content types	Invalid JSON body
Integration Testing	To verify endpoint interaction	Data created by one endpoint is usable by another	Create → Get → Update → Delete
Regression Testing	To protect existing behavior after fixes	Critical API workflows	Booking lifecycle suite
Retesting	To confirm defects are fixed	Failed test cases after fix	Re-run defect-specific tests
Exploratory Testing	To discover undocumented behavior	Edge cases and inconsistent responses	Unsupported methods, extra fields
Contract / Schema Validation	To ensure consumers receive expected structure	JSON/XML fields and data types	bookingid, bookingdates.checkin, depositpaid
Security Testing	Auth and data modification endpoints exist	Basic auth, token misuse, sensitive data exposure	Invalid token, malformed auth header
Performance Testing	Useful for API stability, but NFRs missing	Basic response time observation	Response time baseline only unless SLA is provided

⸻

7. Test Design Techniques

Technique	Application
Equivalence Partitioning	Valid vs invalid usernames, passwords, booking IDs, dates, prices
Boundary Value Analysis	Minimum/maximum string lengths, numeric price values, date boundaries
Decision Table Testing	Auth combinations for PUT/PATCH/DELETE using token, Basic Auth, invalid auth, no auth
State Transition Testing	Booking lifecycle: created → retrieved → updated → patched → deleted
Error Guessing	Malformed JSON, missing nested bookingdates, unsupported content type, invalid Accept header
Pairwise Testing	Combine content type, accept header, auth type, and payload format efficiently
Exploratory Testing	Test undocumented behavior, unexpected fields, duplicate bookings, inconsistent status codes

⸻

8. Test Scenarios

8.1 Auth Module

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
AUTH-TS-001	POST /auth	Verify token is generated with valid username and password	Positive	High
AUTH-TS-002	POST /auth	Verify authentication fails with invalid username	Negative	High
AUTH-TS-003	POST /auth	Verify authentication fails with invalid password	Negative	High
AUTH-TS-004	POST /auth	Verify behavior when username is missing	Negative	High
AUTH-TS-005	POST /auth	Verify behavior when password is missing	Negative	High
AUTH-TS-006	POST /auth	Verify response schema contains token field for successful auth	Contract	High
AUTH-TS-007	POST /auth	Verify unsupported content type is handled correctly	Negative	Medium

⸻

8.2 Get Booking IDs

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
GBID-TS-001	GET /booking	Verify all booking IDs are returned	Positive	High
GBID-TS-002	GET /booking	Verify response is an array of objects containing bookingid	Contract	High
GBID-TS-003	GET /booking?firstname=&lastname=	Verify filtering by firstname and lastname	Positive	High
GBID-TS-004	GET /booking?checkin=&checkout=	Verify filtering by checkin and checkout dates	Positive	High
GBID-TS-005	GET /booking	Verify response when no booking matches filter	Negative	Medium
GBID-TS-006	GET /booking?checkin=invalid-date	Verify invalid date filter behavior	Negative	Medium
GBID-TS-007	GET /booking?firstname=<special_chars>	Verify special characters in filter values	Boundary / Negative	Medium

⸻

8.3 Get Booking by ID

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
GETB-TS-001	GET /booking/:id	Verify booking details are returned for a valid booking ID	Positive	High
GETB-TS-002	GET /booking/:id	Verify response schema contains firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds	Contract	High
GETB-TS-003	GET /booking/:id	Verify JSON response when Accept: application/json is sent	Positive	High
GETB-TS-004	GET /booking/:id	Verify XML response when Accept: application/xml is sent	Positive	Medium
GETB-TS-005	GET /booking/:id	Verify behavior for non-existent booking ID	Negative	High
GETB-TS-006	GET /booking/:id	Verify behavior for invalid ID format	Negative	Medium
GETB-TS-007	GET /booking/:id	Verify behavior when unsupported Accept header is sent	Negative	Medium

⸻

8.4 Create Booking

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
CRTB-TS-001	POST /booking	Verify booking is created with valid JSON payload	Positive	High
CRTB-TS-002	POST /booking	Verify booking is created with valid XML payload	Positive	Medium
CRTB-TS-003	POST /booking	Verify booking is created with valid form-urlencoded payload	Positive	Medium
CRTB-TS-004	POST /booking	Verify response contains bookingid and nested booking object	Contract	High
CRTB-TS-005	POST /booking	Verify created booking can be retrieved using returned bookingid	Integration	High
CRTB-TS-006	POST /booking	Verify behavior when firstname is missing	Negative	High
CRTB-TS-007	POST /booking	Verify behavior when lastname is missing	Negative	High
CRTB-TS-008	POST /booking	Verify behavior when totalprice is non-numeric	Negative	High
CRTB-TS-009	POST /booking	Verify behavior when depositpaid is not boolean	Negative	Medium
CRTB-TS-010	POST /booking	Verify behavior when checkin date is invalid	Negative	High
CRTB-TS-011	POST /booking	Verify behavior when checkout date is before checkin date	Boundary	High
CRTB-TS-012	POST /booking	Verify behavior with special characters in guest name and additional needs	Boundary	Medium
CRTB-TS-013	POST /booking	Verify behavior with very large totalprice value	Boundary	Medium
CRTB-TS-014	POST /booking	Verify behavior with malformed JSON	Negative	High

⸻

8.5 Full Update Booking

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
UPDB-TS-001	PUT /booking/:id	Verify booking is fully updated with valid token in Cookie header	Positive	High
UPDB-TS-002	PUT /booking/:id	Verify booking is fully updated with valid Basic Auth header	Positive	High
UPDB-TS-003	PUT /booking/:id	Verify updated values are returned in response	Contract	High
UPDB-TS-004	PUT /booking/:id	Verify updated booking can be retrieved using GET	Integration	High
UPDB-TS-005	PUT /booking/:id	Verify update fails when authentication is missing	Security / Negative	High
UPDB-TS-006	PUT /booking/:id	Verify update fails when token is invalid	Security / Negative	High
UPDB-TS-007	PUT /booking/:id	Verify behavior when booking ID does not exist	Negative	High
UPDB-TS-008	PUT /booking/:id	Verify behavior when mandatory field is missing in full update	Negative	High
UPDB-TS-009	PUT /booking/:id	Verify behavior with invalid data types	Negative	Medium
UPDB-TS-010	PUT /booking/:id	Verify XML update with valid Basic Auth	Positive	Medium
UPDB-TS-011	PUT /booking/:id	Verify form-urlencoded update with valid Basic Auth	Positive	Medium

⸻

8.6 Partial Update Booking

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
PATB-TS-001	PATCH /booking/:id	Verify firstname and lastname can be partially updated	Positive	High
PATB-TS-002	PATCH /booking/:id	Verify only submitted fields are changed and other fields remain unchanged	Integration	High
PATB-TS-003	PATCH /booking/:id	Verify partial update using token authentication	Positive	High
PATB-TS-004	PATCH /booking/:id	Verify partial update using Basic Auth	Positive	High
PATB-TS-005	PATCH /booking/:id	Verify partial update fails without authentication	Security / Negative	High
PATB-TS-006	PATCH /booking/:id	Verify partial update fails with invalid token	Security / Negative	High
PATB-TS-007	PATCH /booking/:id	Verify behavior when invalid field type is submitted	Negative	Medium
PATB-TS-008	PATCH /booking/:id	Verify behavior with empty payload	Negative / Exploratory	Medium
PATB-TS-009	PATCH /booking/:id	Verify behavior for non-existent booking ID	Negative	High

⸻

8.7 Delete Booking

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
DELB-TS-001	DELETE /booking/:id	Verify booking is deleted with valid token in Cookie header	Positive	High
DELB-TS-002	DELETE /booking/:id	Verify booking is deleted with valid Basic Auth header	Positive	High
DELB-TS-003	DELETE /booking/:id	Verify deleted booking is no longer retrievable	Integration	High
DELB-TS-004	DELETE /booking/:id	Verify delete fails without authentication	Security / Negative	High
DELB-TS-005	DELETE /booking/:id	Verify delete fails with invalid token	Security / Negative	High
DELB-TS-006	DELETE /booking/:id	Verify behavior when deleting already deleted booking	Negative	Medium
DELB-TS-007	DELETE /booking/:id	Verify behavior when booking ID format is invalid	Negative	Medium

⸻

8.8 Ping / Health Check

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
PING-TS-001	GET /ping	Verify health check endpoint returns successful response	Smoke	High
PING-TS-002	GET /ping	Verify expected status code is 201 Created as documented	Contract	High
PING-TS-003	GET /ping	Verify response time is within acceptable baseline	Performance Baseline	Medium

⸻

8.9 End-to-End Scenarios

Scenario ID	Feature / Endpoint	Scenario Description	Type	Priority
E2E-TS-001	Booking lifecycle	Auth → Create booking → Get booking → Update booking → Get booking → Delete booking → Verify deletion	Regression	High
E2E-TS-002	Partial update lifecycle	Create booking → Patch selected fields → Verify changed and unchanged fields	Integration	High
E2E-TS-003	Auth lifecycle	Generate token → use token for PUT/PATCH/DELETE	Security / Integration	High

⸻

9. Test Data Strategy

9.1 Valid Test Data

{
  "firstname": "Jim",
  "lastname": "Brown",
  "totalprice": 111,
  "depositpaid": true,
  "bookingdates": {
    "checkin": "2018-01-01",
    "checkout": "2019-01-01"
  },
  "additionalneeds": "Breakfast"
}

9.2 Valid Authentication Data

{
  "username": "admin",
  "password": "password123"
}

9.3 Invalid and Boundary Test Data

Field	Valid Data	Invalid / Boundary Data
firstname	Jim	blank, null, numeric, special characters, very long string
lastname	Brown	blank, null, numeric, special characters, very long string
totalprice	111	0, -1, decimal, string, null, very large number
depositpaid	true, false	"true", 1, null, blank
checkin	2018-01-01	invalid format, blank, null, impossible date
checkout	2019-01-01	date before checkin, invalid format, null
additionalneeds	Breakfast	blank, null, special characters, large text
bookingid	Existing numeric ID	non-existent ID, deleted ID, string, negative number
Auth token	Valid generated token	expired token, malformed token, missing token
Basic Auth	Valid encoded credentials	invalid credentials, malformed header

9.4 Data Management Approach

Data Need	Approach
Existing booking ID	Use GET /booking or create new booking before test
Isolated update/delete tests	Create test booking during setup
Cleanup	Delete test-created bookings where possible
Regression data	Use data-driven test files for JSON/XML/form payloads
Negative testing	Use controlled invalid payloads and non-existent IDs

⸻

10. Environment Requirements

Environment	Base URL	Build / Version	Purpose	Notes
QA / Test	https://restful-booker.herokuapp.com	Not specified	Functional and regression testing	Public playground API
Local Automation	Same as above	Not specified	API automation execution	Requires internet access
CI Environment	Same as above	Not specified	Automated smoke/regression execution	Requires configured secrets and network access

Tools Required

Tool	Purpose
Postman	Manual API testing and collection creation
Newman	Command-line execution of Postman collections
REST Assured	API automation framework
Java	REST Assured test development
TestNG / JUnit	Test orchestration
Maven / Gradle	Build and dependency management
GitHub Actions / Jenkins	CI/CD execution
Allure / Extent Reports	Automation reporting
JIRA	Defect tracking
Git	Version control
JSON Schema Validator	Contract validation

Access Requirements

Access Item	Requirement
API Base URL	Must be reachable from tester machine and CI agent
Auth Credentials	admin/password123 as documented
Network	Internet access required
Test Data	Ability to create and delete bookings

⸻

11. Entry Criteria

Testing may begin when:

Criteria ID	Entry Criteria
EC-001	API documentation is available and reviewed
EC-002	Base URL is accessible
EC-003	Auth credentials are available
EC-004	Test environment is stable
EC-005	Test data strategy is agreed
EC-006	Postman or automation tools are configured
EC-007	Smoke test using /ping passes
EC-008	QA team has reviewed known documentation gaps and assumptions

⸻

12. Exit Criteria

Testing may be considered complete when:

Criteria ID	Exit Criteria
XC-001	All planned high-priority test scenarios are executed
XC-002	Critical and high severity defects are fixed, retested, or formally accepted
XC-003	Regression suite has passed
XC-004	Defect report is updated
XC-005	Test execution report is prepared
XC-006	Known limitations and open risks are documented
XC-007	Stakeholder sign-off is received

⸻

13. Defect Management Process

13.1 Defect Reporting Tool

Recommended tool: JIRA

13.2 Defect Lifecycle

New → Assigned → In Progress → Fixed → Ready for Retest → Retest Passed → Closed
                                      ↓
                                   Reopened

13.3 Severity Levels

Severity	Meaning	Example
Critical	Core API unusable or data corruption	Booking cannot be created
High	Major function broken	PUT allows update without authentication
Medium	Functional issue with workaround	XML response has missing optional field
Low	Minor inconsistency	Documentation typo or unclear message

13.4 Priority Levels

Priority	Meaning
P1	Must fix immediately
P2	Must fix before release
P3	Fix in upcoming sprint
P4	Cosmetic or low business impact

13.5 Defect Template

Field	Details
Defect ID	Auto-generated
Title	Short description
Environment	QA / Test
Endpoint	API path and method
Steps to Reproduce	Numbered steps
Expected Result	Expected API behavior
Actual Result	Observed API behavior
Test Data	Payload, booking ID, auth token type
Request Details	Method, URL, headers, body
Response Details	Status code, headers, body
Logs / Evidence	Postman screenshot, Newman report, console log
Severity	Critical / High / Medium / Low
Priority	P1 / P2 / P3 / P4
Assigned To	Developer / Team
Status	Current lifecycle status

⸻

14. Test Deliverables

Deliverable	Description
Test Plan	This document
Requirement Traceability Matrix	Mapping of API requirements to test scenarios
Test Scenarios	High-level scenario list
Test Cases	Detailed executable test cases
Test Data	Valid, invalid, and boundary data
Postman Collection	Manual and smoke API collection
Postman Environment File	Base URL, token, booking ID variables
REST Assured Automation Suite	Automated smoke and regression tests
Defect Reports	Logged defects with evidence
Test Execution Report	Pass/fail status and metrics
Regression Report	Regression execution results
Test Closure Report	Summary, risks, defects, sign-off

⸻

15. Test Execution Plan

Phase	Activity	Owner	Start Date	End Date	Dependency	Deliverable
Phase 1	Requirement and API documentation review	QA Lead / QA Engineer	TBD	TBD	API documentation	Requirement review notes
Phase 2	Test scenario preparation	QA Engineer	TBD	TBD	Requirement analysis	Test scenarios
Phase 3	Test case design	QA Engineer	TBD	TBD	Approved scenarios	Test cases
Phase 4	Postman collection creation	QA Engineer	TBD	TBD	Test cases	Postman collection
Phase 5	Smoke testing	QA Engineer	TBD	TBD	Environment readiness	Smoke report
Phase 6	Functional API testing	QA Engineer	TBD	TBD	Smoke pass	Execution report
Phase 7	Negative and security testing	QA Engineer	TBD	TBD	Functional setup	Defect report
Phase 8	Integration / lifecycle testing	QA Engineer	TBD	TBD	Test data	E2E execution report
Phase 9	Automation scripting	SDET	TBD	TBD	Stable scenarios	REST Assured suite
Phase 10	Regression testing	QA / SDET	TBD	TBD	Defect fixes	Regression report
Phase 11	Final closure and sign-off	QA Lead	TBD	TBD	Execution completion	Test closure report

⸻

16. Automation Strategy

16.1 Recommended Automation Scope

Area	Automate?	Reason
/ping smoke check	Yes	Fast build health validation
/auth token generation	Yes	Required for protected endpoint automation
Create booking	Yes	Core regression flow
Get booking by ID	Yes	Core validation and setup verification
Get booking IDs	Yes	Core API behavior
Full update booking	Yes	Protected endpoint regression
Partial update booking	Yes	Protected endpoint regression
Delete booking	Yes	Cleanup and lifecycle validation
Negative schema/data tests	Partially	Automate stable and repeatable cases
Exploratory testing	No	Human-driven investigation
Documentation review	No	Manual validation

16.2 Framework Recommendation

Layer	Tool
Manual API Testing	Postman
CLI Execution	Newman
Automation Framework	REST Assured
Language	Java
Test Runner	TestNG or JUnit
Build Tool	Maven or Gradle
Reporting	Allure or Extent Reports
CI/CD	GitHub Actions or Jenkins
Version Control	Git

16.3 Automation Design

Component	Description
Base Test Class	Handles base URI, common headers, logging
Auth Utility	Generates and stores token
Booking Payload Builder	Creates reusable valid and invalid payloads
Schema Validators	Validates JSON/XML response structure
Test Data Provider	Supplies boundary and negative data
Cleanup Utility	Deletes created test bookings
Reporting Layer	Captures request, response, status, and assertion details

16.4 Suggested Automated Suites

Suite	Coverage
Smoke Suite	/ping, /auth, create booking, get booking
Regression Suite	Full booking lifecycle
Security Suite	Missing/invalid auth for PUT/PATCH/DELETE
Contract Suite	Schema validation for major responses
Data Validation Suite	Invalid payloads and boundary values

⸻

17. Non-Functional Testing Considerations

Area	Applicability	Planned Coverage
Performance Testing	Limited, because no SLA is documented	Capture baseline response times for key endpoints
Load Testing	Requires additional NFRs	Not planned unless concurrency/throughput targets are provided
Security Testing	Applicable	Auth bypass, invalid token, Basic Auth misuse, sensitive data exposure
Reliability Testing	Applicable	/ping availability and repeated request stability
Compatibility Testing	API-level only	Validate JSON, XML, and form-urlencoded support
Observability / Logging	Not documented	Out of scope unless logs or monitoring access is provided

⸻

18. Risks and Mitigations

Risk ID	Risk Description	Impact	Probability	Mitigation	Owner
RISK-001	API documentation does not define all negative status codes	Medium	High	Capture observed behavior and raise documentation gaps	QA Lead
RISK-002	Public playground API data may change due to other users	High	High	Create isolated test data during execution	QA Engineer
RISK-003	Environment may be unstable or unavailable	High	Medium	Run /ping before execution and retry failed infra cases	QA Engineer
RISK-004	Auth token behavior such as expiry is not documented	Medium	Medium	Generate fresh token per test run	SDET
RISK-005	DELETE may remove shared test data	Medium	Medium	Delete only bookings created by automation	QA Engineer
RISK-006	Lack of clear validation rules for field lengths and date logic	Medium	High	Mark as open questions and perform exploratory testing	QA Lead
RISK-007	Flaky tests due to shared environment	High	Medium	Add retries only for environment errors, not assertion failures	SDET
RISK-008	XML and form-urlencoded behavior may differ from JSON	Medium	Medium	Include format-specific tests	QA Engineer
RISK-009	No performance SLA is available	Low	High	Report baseline only, do not certify performance	QA Lead
RISK-010	Inconsistency in documentation for PATCH examples showing PUT command	Medium	High	Log documentation issue and verify actual endpoint behavior	QA Engineer

⸻

19. Assumptions

ID	Assumption
ASM-001	The base URL is https://restful-booker.herokuapp.com.
ASM-002	The documented credentials admin/password123 are valid for token generation.
ASM-003	POST /booking does not require authentication.
ASM-004	PUT, PATCH, and DELETE require either Cookie token or Basic Auth.
ASM-005	The test team may create and delete test bookings in the public playground environment.
ASM-006	Status code and schema expectations are based on the provided API documentation.
ASM-007	Error status codes not documented will be discovered during testing and treated as actual behavior until clarified.
ASM-008	No UI, mobile, browser, or accessibility testing is required.
ASM-009	Automation will generate new booking data instead of relying on fixed existing booking IDs.

⸻

20. Dependencies

Dependency ID	Dependency
DEP-001	API documentation availability
DEP-002	Restful Booker API environment availability
DEP-003	Valid authentication credentials
DEP-004	Ability to create booking records
DEP-005	Ability to delete test-created booking records
DEP-006	Postman and automation tool setup
DEP-007	CI/CD access if automation is integrated
DEP-008	Stable network connectivity
DEP-009	Stakeholder clarification for undocumented validation and error handling rules

⸻

21. Open Questions

Question ID	Open Question
OQ-001	What are the expected status codes for invalid credentials, missing fields, malformed payloads, and invalid IDs?
OQ-002	Are firstname, lastname, totalprice, depositpaid, bookingdates.checkin, and bookingdates.checkout mandatory for create booking?
OQ-003	What are the maximum allowed lengths for firstname, lastname, and additionalneeds?
OQ-004	Should checkout date be strictly greater than checkin date?
OQ-005	Is totalprice allowed to be zero, negative, or decimal?
OQ-006	Does the auth token expire? If yes, what is the expiry duration?
OQ-007	Should unsupported Content-Type or Accept headers return 415, 406, or another status?
OQ-008	What should happen when extra fields are submitted in the booking payload?
OQ-009	What is the expected response when deleting a non-existent booking?
OQ-010	The documentation section for partial update says PATCH, but examples show curl -X PUT; should the method be PATCH or PUT?
OQ-011	The delete booking section says success is “Default HTTP 201 response,” while DELETE usually returns 201, 200, or 204 depending on implementation. What is the expected status?
OQ-012	Are XML and form-urlencoded formats required for all booking create/update/partial update operations or only examples?

⸻

22. Approval / Sign-Off

Name	Role	Approval Status	Date	Comments
TBD	Product Owner	Pending	TBD	
TBD	Engineering Lead	Pending	TBD	
TBD	QA Lead	Pending	TBD	
TBD	Test Architect	Pending	TBD	

⸻

Requirement Traceability Matrix

Requirement ID	Endpoint / Feature	Test Scenario IDs	Test Type
RB-AUTH-001	POST /auth	AUTH-TS-001 to AUTH-TS-007	Functional, Negative, Security, Contract
RB-BOOK-001	GET /booking	GBID-TS-001 to GBID-TS-007	Functional, Data Validation, Contract
RB-BOOK-002	GET /booking/:id	GETB-TS-001 to GETB-TS-007	Functional, Negative, Contract
RB-BOOK-003	POST /booking	CRTB-TS-001 to CRTB-TS-014	Functional, Boundary, Negative, Integration
RB-BOOK-004	PUT /booking/:id	UPDB-TS-001 to UPDB-TS-011	Functional, Security, Regression
RB-BOOK-005	PATCH /booking/:id	PATB-TS-001 to PATB-TS-009	Functional, Security, Integration
RB-BOOK-006	DELETE /booking/:id	DELB-TS-001 to DELB-TS-007	Functional, Security, Integration
RB-PING-001	GET /ping	PING-TS-001 to PING-TS-003	Smoke, Contract, Reliability
RB-E2E-001	Booking lifecycle	E2E-TS-001 to E2E-TS-003	Integration, Regression, Security

⸻

Recommended Smoke Test Set

Order	Endpoint	Validation
1	GET /ping	API is up and returns documented success response
2	POST /auth	Token is generated
3	GET /booking	Booking ID list is returned
4	POST /booking	Booking is created and bookingid is returned
5	GET /booking/:id	Created booking is retrievable
6	DELETE /booking/:id	Created booking can be cleaned up using auth

This test plan is ready to be converted into detailed test cases, a Postman collection, and a REST Assured automation framework.