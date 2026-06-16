# Task Plan — JIRA Test Plan Generator

## Objective
Fetch a JIRA ticket by ID (e.g., VWO-48), pass its full content to Claude, and generate a structured Test Plan as a Markdown + PDF file.

---

## Phase Checklist

### Phase 0: Initialization ✅
- [x] Create `task_plan.md`
- [x] Create `findings.md`
- [x] Create `progress.md`
- [x] Create `LLM.md` (Project Constitution)

### Phase 1: Blueprint (Discovery) 🔄 IN PROGRESS
- [x] North Star defined — generate test plan from JIRA ticket
- [x] Integrations confirmed — JIRA + Claude API
- [x] Source of Truth confirmed — full JIRA ticket (SCRUM-6)
- [x] Delivery Payload confirmed — Markdown + PDF file
- [ ] Behavioral Rules — test plan structure/sections (PENDING USER INPUT)
- [ ] JIRA base URL (PENDING USER INPUT)
- [ ] Output directory location (PENDING USER INPUT)
- [ ] Sub-tasks / linked issues scope (PENDING USER INPUT)
- [ ] Data Schema defined in `LLM.md`
- [ ] Blueprint approved

### Phase 2: Link (Connectivity)
- [ ] JIRA API connection verified
- [ ] Claude API connection verified
- [ ] `.env` file populated and tested

### Phase 3: Architect (Build)
- [ ] `architecture/` SOPs written
- [ ] `tools/fetch_jira_ticket.py` — fetches raw JIRA ticket data
- [ ] `tools/generate_test_plan.py` — sends ticket to Claude, returns test plan
- [ ] `tools/export_output.py` — writes Markdown + PDF to output directory
- [ ] `.tmp/` directory initialized

### Phase 4: Stylize (Refinement)
- [ ] Test plan Markdown format reviewed
- [ ] PDF export formatting verified
- [ ] User review and feedback

### Phase 5: Trigger (Deployment)
- [ ] Final script wired end-to-end
- [ ] Trigger mechanism defined (CLI, cron, or webhook)
- [ ] `LLM.md` Maintenance Log finalized

---

## Goals
1. Given a JIRA ID (e.g., `VWO-48`), fetch the full ticket from JIRA REST API
2. Send the ticket content (summary, description, acceptance criteria, labels, priority) to Claude
3. Claude generates a structured test plan
4. Output is saved as `{JIRA_ID}_test_plan.md` and `{JIRA_ID}_test_plan.pdf`
