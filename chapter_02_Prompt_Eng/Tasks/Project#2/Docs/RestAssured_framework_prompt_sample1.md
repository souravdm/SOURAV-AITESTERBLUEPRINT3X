Role: Act as a Senior QA Engineer and using Python requests module generate an API test Automation enterprise grade framework with proper skeleton for the below API documentation.

Task: 
1. Generate API Testing framework for each test casese in ["Functional_Test_Cases.csv"]
2. Use Python request module, pytest and allure for reporting.
3. Create a enterpise grade test-suite framework with dedicated testing folders names {"framework":"Restful-booker-py-framework" "testcases":""} here framework folder has the folder structure of a typical api automation framework and testcases folder has the test cases.
4. Create seperate files for each test cases. 
5. Create Test Happy path testing & negative path testing separately.
6. Create test case considering  boundary conditions.
7. Error code handling.
8. Authentication and authorization tests.
9. Create a test runner file to run all the test cases.
10. Create a test execution report using allure.
11. Make the framework ready for docker deployment.
12. Add test data as JSON files and read the data from the JSON files.
13. Make use of constants for base url, headers, auth, etc.
11. Analyse the PRD [API-booker-test-PRD.pdf] and API Specification [Restful-booker_API_Spec.pdf] and test plan [restful-booker_test_plan.md] and based on these inputs only create test cases.
12. While genrating test cases files, check the mapping of TCs in test case sheet and with PRD.

Constrain:
1. Restict yout self to generate only the code for the framework. Do not generate any other text.
2. Strictly ristrict yourself to only documents provided in Task 11.
3. Don't hallucinate, and genrate your own test cases
4. Don't assume any deafault feature and genrate test case.
5. If your not clear about any test case or unable to map the test case with requirement in PRD, then don't generate the test case. Instead leave it blank and ask me for clarification.