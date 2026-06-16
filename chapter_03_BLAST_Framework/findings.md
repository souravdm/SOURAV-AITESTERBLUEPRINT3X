# Findings — JIRA Test Plan Generator

## Research & Discoveries

### JIRA REST API
- JIRA Cloud REST API v3 endpoint for a single issue: `GET /rest/api/3/issue/{issueIdOrKey}`
- Authentication: Basic Auth using email + API token (`Authorization: Basic base64(email:token)`)
- Full ticket fields available: `summary`, `description`, `issuetype`, `priority`, `labels`, `status`, `assignee`, `reporter`, `subtasks`, `issuelinks`, `comment`, `acceptance criteria` (often a custom field)
- Description is returned in Atlassian Document Format (ADF) — requires parsing to plain text
- API Docs: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-get

### Claude API
- Model to use: `claude-sonnet-4-6` (latest Sonnet, cost-effective for long document generation)
- SDK: `anthropic` Python package
- Max context: 200K tokens — sufficient for large JIRA tickets
- Structured output via prompt engineering (no function calling needed for markdown generation)

### PDF Generation (Python)
- Options: `markdown-pdf`, `weasyprint`, `fpdf2`, `reportlab`
- Recommended: `weasyprint` — best CSS/HTML → PDF fidelity; or `markdown2` + `weasyprint` pipeline
- Alternative: `mdpdf` CLI tool for simple markdown → PDF

### Constraints Identified
- JIRA description in ADF format — need `atlassian-python-api` or custom ADF parser to extract plain text
- Claude API key required in `.env`
- JIRA base URL and API token required in `.env`

---

## Open Questions (Awaiting User Input)
- [ ] Test plan sections/structure (Behavioral Rules)
- [ ] JIRA base URL
- [ ] Output directory
- [ ] Sub-tasks/linked issues scope
