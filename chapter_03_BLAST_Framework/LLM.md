# LLM.md — Project Constitution
# JIRA Test Plan Generator

> This file is LAW. Update only when: a schema changes, a rule is added, or architecture is modified.

---

## 1. Data Schema

### Input — Raw JIRA Ticket (fetched from API)
```json
{
  "jira_id": "SCRUM-6",
  "summary": "string",
  "description": "string (parsed from ADF to plain text)",
  "issue_type": "string (Story | Bug | Task | Epic)",
  "priority": "string (Highest | High | Medium | Low | Lowest)",
  "status": "string",
  "labels": ["string"],
  "assignee": "string | null",
  "reporter": "string",
  "acceptance_criteria": "string | null",
  "subtasks": [
    {
      "id": "string",
      "summary": "string",
      "status": "string"
    }
  ],
  "linked_issues": [
    {
      "id": "string",
      "summary": "string",
      "link_type": "string"
    }
  ]
}
```

### Intermediate — Claude Prompt Payload
```json
{
  "model": "claude-sonnet-4-6",
  "max_tokens": 4096,
  "system": "string (test plan generation system prompt)",
  "messages": [
    {
      "role": "user",
      "content": "string (formatted JIRA ticket context)"
    }
  ]
}
```

### Output — Generated Test Plan
```json
{
  "jira_id": "Scrum-6",
  "generated_at": "ISO8601 timestamp",
  "markdown_path": "string (path to .md file)",
  "pdf_path": "string (path to .pdf file)",
  "test_plan_content": "string (full markdown text)"
}
```

---

## 2. Behavioral Rules

### Claude Prompt Rules
- Always include the JIRA ID and ticket summary at the top of the test plan
- Generate test cases covering: functional, negative, edge cases, and regression
- Never assume happy path only — always include failure scenarios
- If acceptance criteria are present, map each criterion to at least one test case
- Output must be valid Markdown
- Test cases must have: ID, Title, Preconditions, Steps, Expected Result

### API Rules
- JIRA: Use REST API v3. Auth via Basic Auth (email:token base64 encoded)
- JIRA: Parse ADF description to plain text before sending to Claude
- Claude: Use `claude-sonnet-4-6` model
- Rate limits: Respect JIRA API rate limits (respect `Retry-After` headers)

### File Output Rules
- Output file naming: `{JIRA_ID}_test_plan.md` and `{JIRA_ID}_test_plan.pdf`
- Output directory: TBD (pending user input)
- All intermediate files go to `.tmp/`

---

## 3. Architectural Invariants

- Tools in `tools/` are atomic and independently testable
- No business logic in the navigation/orchestration layer — only tool calls
- `.env` holds all secrets — never hardcode credentials
- `LLM.md` is updated before code changes when schema or rules change
- Architecture SOPs in `architecture/` must be updated before the corresponding tool is changed

---

## 4. Environment Variables (Required)

```
JIRA_BASE_URL=https://yourcompany.atlassian.net
JIRA_EMAIL=your-email@company.com
JIRA_API_TOKEN=your-jira-api-token
ANTHROPIC_API_KEY=your-claude-api-key
OUTPUT_DIR=./output
```

---

## 5. Maintenance Log

| Date | Change | Reason |
|------|--------|--------|
| 2026-06-16 | Initial constitution created | Phase 0 initialization |
