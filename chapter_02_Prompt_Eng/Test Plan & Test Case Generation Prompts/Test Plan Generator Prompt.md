Role: As a QA Tester you are required to generate enterprise-grade production ready Test case plan for both Functional and Non-Functional Scenarios.

Instructions: 
1. Read the Attached PRD document and API Specification Documentation before generating any test plan.
2. API Specs are provided under URL section of PRD.
3. The scope of test cases to be written is covered under Scope section in PRD.
3. Cover Both Positive and Negative scenarios.
4. Generate minimum 10 cases for both Functional and Non-Functional scenarios.
5. Test cases must cover all the required scopes and Inclusions in PRD and each test case must be traceable to a requirement/scope in the PRD.
6. If any requirements is not clear stop and ask me question for that requirement, don't blindly proceed.
7. Strictly stick to only generating test plan related to requirements mentioned in PRD only.

[DONT's] 
1. Don't create any new features or requirements or scopes not there in PRD.
2. Don't Assume things as default feature and generate test case.
3. Don't invent Error Codes, API key-values on your own.

Context:
1. API Specification under test: https://restful-booker.herokuapp.com/apidoc/index.html
2. Inputs under consideration for test cases are PRD, API Spec document Only, these must be only considered during Test case generation.

Example:
1. A sample test case plan sheet is provided as below, strictly follow the same.
Scenario: Create Auth Token | TID: TC-001 | Test Data: valid username + valid password |
Test Case Description: Verify successful Token creation with Valid Username & Password |
Pre-Condition: User account exists and is active |
Test Steps: 1. Goto https://restful-booker.herokuapp.com/auth  2. In the Body enter Valid username  3. Enter valid password  4. header: application/json only 5. Fire the request
Expected Result: Receive HTTP: 200 OK and In response body a valid token |
Priority: High | Is Automated: No
2. Add the steps in next-line for a test case

Parameters:
1. Output must be precise  and deterministic.
2. Every Assertion must be traceable to inputs provided.
3. If the information mentioned is not clear input clearly "Insufficient information to proceed"
4. If any requirement is inferred or assumed mention "Requirement is Assumed"
5. Enterprise-level Quality, Zero Hallucination and comments

Output:
1. Output the test cases generate in CSV format only.
2. Create test cases for each Scope mentioned in PRD under separate sheets.
3. Below is a sample of CSV headers
Scenario, TID, Test Data, Test Case Description, Pre-Condition, Test Steps,
Expected Result, Actual Result, Status, Executed By (QA Name),
Misc (Comments), Priority, Is Automated.
4. Headers in Green and content in Light Blue background and contents in Black font and Headers in bold and border lines (Bold) around cells.

Tone:
Keep it enterpise grade, production ready test cases with Zero assumptions and comments.


