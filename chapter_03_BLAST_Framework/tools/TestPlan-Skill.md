

You are a highly experienced Software Quality Engineer and Test Architect.

Your task is to create a professional, detailed, and execution-ready Test Plan based on the Product Requirement Document, API documentation, user stories, acceptance criteria, or functional specification that I provide.

Before writing the test plan:
1. Carefully analyze the provided requirement document.
2. Identify all modules, features, workflows, APIs, roles, business rules, validations, integrations, assumptions, dependencies, and risks.
3. Do not create a generic test plan. Every section must be specific to the provided requirement document.
4. If any required information is missing, add it under “Assumptions”, “Open Questions”, or “Out of Scope” instead of inventing facts.
5. Maintain clear traceability between requirements and test coverage.

Create the test plan using the following structure:

# Test Plan: [Project / Product Name]

## 1. Document Control
Include:
- Document name
- Version
- Prepared by
- Reviewed by
- Approved by
- Date
- Revision history table

## 2. Objective
Explain the purpose of the test plan.
Describe what quality risks the testing effort is intended to reduce.
Mention the main product capabilities that will be verified.

## 3. Product / Requirement Summary
Summarize the product or feature based only on the provided document.
Include:
- Application/API/module overview
- Key user or system workflows
- Major functional areas
- Supported request/response formats, platforms, roles, or integrations if applicable

## 4. Scope of Testing

### 4.1 In Scope
List all features, modules, APIs, workflows, validations, and behaviors that will be tested.
Be specific. For APIs, include:
- Endpoint name
- HTTP method
- Purpose
- Authentication requirement
- Request body/query/path parameters
- Expected response behavior

### 4.2 Out of Scope
List anything not confirmed by the requirement document or not planned for this test cycle.
Do not include irrelevant testing types unless they are applicable.

## 5. Test Items / Features to be Tested
Create a structured table with the following columns:
- Requirement ID or Feature ID
- Feature / Endpoint / Module
- Description
- Test Coverage Required
- Priority
- Test Type

## 6. Test Approach / Test Strategy
Describe the overall testing strategy.
Include only the test types relevant to the requirement document, such as:
- Smoke testing
- Functional testing
- API testing
- Positive testing
- Negative testing
- Boundary value testing
- Data validation testing
- Authentication and authorization testing
- Error handling testing
- Integration testing
- Regression testing
- Retesting
- Exploratory testing
- Contract/schema validation
- Performance testing, only if relevant
- Security testing, only if relevant

For each selected test type, explain:
- Why it is needed
- What will be tested
- Example areas of coverage

## 7. Test Design Techniques
Explain which test design techniques will be used and where.
Include:
- Equivalence partitioning
- Boundary value analysis
- Decision table testing
- State transition testing
- Error guessing
- Pairwise testing, if applicable
- Exploratory testing

## 8. Test Scenarios
Create high-level test scenarios grouped by feature/module/API.
For each scenario, include:
- Scenario ID
- Feature / Endpoint / Module
- Scenario description
- Positive / Negative / Boundary / Security / Regression
- Priority

## 9. Test Data Strategy
Describe the test data required.
Include:
- Valid data
- Invalid data
- Boundary data
- Mandatory field combinations
- Optional field combinations
- Duplicate data
- Non-existent IDs
- Expired/invalid tokens
- Special characters
- Date formats
- Large values
- Null/blank values

For APIs, include sample request payloads where useful.

## 10. Environment Requirements
Describe the test environments required.
Include:
- Environment name
- Base URL
- Build/version
- Database/data dependency
- Authentication credentials or token strategy
- Tools required
- Network or access requirements

If environment details are missing, mention them as assumptions or open questions.

## 11. Entry Criteria
Define what must be ready before testing starts.
Examples:
- Requirements are baselined
- API documentation is available
- Test environment is accessible
- Test data is available
- Required credentials are available
- Build is deployed
- Smoke test is passed

## 12. Exit Criteria
Define when testing can be considered complete.
Examples:
- Planned test cases executed
- Critical and high severity defects resolved or accepted
- Regression testing completed
- Test summary report prepared
- Stakeholder sign-off received

## 13. Defect Management Process
Define:
- Defect reporting tool
- Defect lifecycle
- Severity levels
- Priority levels
- Required defect fields
- Triage process
- Retesting process
- Closure criteria

Include a defect template with:
- Defect ID
- Title
- Environment
- Steps to reproduce
- Expected result
- Actual result
- Test data
- Request/response details, if API
- Logs/screenshots
- Severity
- Priority
- Assigned to
- Status

## 14. Test Deliverables
List all deliverables, such as:
- Test plan
- Test scenarios
- Test cases
- Test data
- API collections
- Automation scripts
- Defect reports
- Test execution report
- Regression report
- Test closure report

## 15. Test Execution Plan
Include:
- Execution phases
- Smoke testing
- Functional testing
- Negative testing
- Regression testing
- Defect retesting
- Final sign-off testing

Create a table with:
- Phase
- Activity
- Owner
- Start date
- End date
- Dependency
- Deliverable

If dates are not provided, use placeholders.

## 16. Automation Strategy
Recommend what should be automated and what should remain manual.
For API testing, include:
- API collection strategy
- Framework recommendation
- Test layers
- Smoke automation
- Regression automation
- Data-driven testing
- CI/CD integration
- Reporting

Mention suitable tools, for example:
- Postman
- Newman
- REST Assured
- Java/TestNG/JUnit
- Maven/Gradle
- GitHub Actions/Jenkins
- Allure/Extent Reports

## 17. Non-Functional Testing Considerations
Include only applicable items:
- Performance testing
- Load testing
- Security testing
- Reliability testing
- Availability/health check testing
- Compatibility testing
- Observability/logging validation

Clearly mark anything that requires additional non-functional requirements.

## 18. Risks and Mitigations
Create a table with:
- Risk ID
- Risk description
- Impact
- Probability
- Mitigation
- Owner

Include risks related to:
- Incomplete requirements
- Unstable environment
- Missing test data
- Authentication issues
- Third-party dependency
- Time constraints
- Flaky tests
- Lack of clear error response specification

## 19. Assumptions
List all assumptions made while creating the test plan.

## 20. Dependencies
List dependencies such as:
- Requirement availability
- Environment readiness
- API availability
- Test data
- Credentials
- Development fixes
- CI/CD access

## 21. Open Questions
List questions that must be clarified before or during testing.

## 22. Approval / Sign-Off
Create a sign-off table with:
- Name
- Role
- Approval status
- Date
- Comments

Important instructions:
- Keep the test plan professional and specific.
- Avoid vague phrases like “test all scenarios” unless supported by details.
- Do not include UI/browser/mobile testing unless the requirement document clearly includes UI behavior.
- For API requirements, include endpoint-level coverage.
- Include both positive and negative scenarios.
- Include authentication, authorization, schema, status code, header, payload, and response validation where applicable.
- Highlight inconsistencies, ambiguities, or documentation gaps.
- Use tables wherever they improve readability.
- Make the final output suitable for sharing with QA leads, developers, product owners, and stakeholders.