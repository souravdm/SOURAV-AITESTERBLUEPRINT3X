# Progress Log — JIRA Test Plan & Strategy Generator

> Maintained per B.L.A.S.T. Protocol: log every meaningful action, error, fix, and result.

---

## 2026-06-16 — Phase B (Blueprint) — Initialization & Discovery

### Done
- Read and internalized `tools/B.L.A.S.T.md` and `tools/Objective.md`
- Created all Phase 0 memory files: `task_plan.md`, `findings.md`, `progress.md`, `LLM.md`
- Completed Blueprint discovery with user — confirmed integrations (JIRA + Claude), output format (Markdown + PDF consideration), and behavioral rules
- Defined test plan structure: 22 sections from `TestPlan-Skill.md` (to be created)
- JIRA base URL confirmed: `https://souravdm.atlassian.net`

### Result
Project memory initialized. Blueprint approved. Ready for Link phase.

### Errors / Blockers
- None

---

## 2026-06-16 — Phase L (Link) — API Connectivity Verified

### Done
- Confirmed JIRA Cloud REST API v3 connects with Basic Auth (email + API token)
- Confirmed child issues fetchable via JQL: `parent = {epicId} ORDER BY created ASC`
- Confirmed JIRA description is in ADF (Atlassian Document Format) — custom recursive parser required
- Confirmed Claude API (`claude-sonnet-4-6`) accepts up to 200K input tokens and produces up to 8 192 output tokens per call
- Confirmed Anthropic API key must be funded separately from Claude Pro subscription

### Result
Both external links verified. ADF parsing requirement discovered and documented in `findings.md`.

### Errors / Blockers
- None — all endpoints responding correctly

---

## 2026-06-16 — Phase A (Architect) — React App Built

### Done
- Scaffolded React 18 + Vite frontend + Express.js backend in `react-app/`
- Created `server.js` with two endpoints:
  - `GET /api/epic/:epicId` — fetches epic + child issues from JIRA, parses ADF descriptions
  - `POST /api/generate-test-plan` — sends epic data to Claude and returns test plan Markdown
- Created React components: `App.jsx`, `EpicDetails.jsx`, `TestPlan.jsx`
- Applied dark-theme CSS (`App.css`) — sticky header, two-panel split layout, Markdown styled body
- Integrated `TestPlan-Skill.md` as Claude system prompt (`TEST_PLAN_SKILL`) loaded via `readFileSync` at server startup
- Configured Vite proxy (`/api` → `http://localhost:3001`) to eliminate CORS issues

### Result
App runs end-to-end: fetch JIRA epic → display details → generate test plan → render Markdown → download `.md`.

### Errors / Blockers
**Error:** `Failed to resolve import "react/jsx-dev-runtime"` on first `npm run dev`
- **Root Cause:** `react` and `react-dom` were missing from `package.json` dependencies
- **Fix:** Added `"react": "^18.3.1"` and `"react-dom": "^18.3.1"` to `dependencies` and ran `npm install`

---

## 2026-06-16 — Phase A (Architect) — Token Budget Engineering

### Problem
Test plan generation only covered 2 of 17 child issues. Root cause: a single `max_tokens: 8192` call was consumed by the 22-section structure before reaching most test scenarios (Section 8).

### Iteration 1 — Split Structure from Scenarios
Introduced two parallel Claude calls:
- Call A: Sections 1–7 (structure)
- Call B: Section 8 (all scenarios) + Sections 9–22 (tail)

**Result:** Coverage improved but sections 17–22 (Non-Functional Testing, Risks, Assumptions, Dependencies) were still truncated — the combined sections 8–22 still exceeded 8 192 tokens.

### Iteration 2 — Split Tail into C1 + C2
Split tail into two dedicated calls:
- Call C1: Sections 9–16 (Test Data → Automation Strategy)
- Call C2: Sections 17–22 (Non-Functional → Sign-Off)

**Result:** All 22 sections generated, but total content was shorter than Iteration 1.

### Iteration 3 — Two Context Sizes (final architecture)
**Root Cause of regression:** Tail calls (C1, C2) were receiving full child descriptions (~2 000 input tokens wasted) leaving less output budget.

**Fix:** Introduced two context shapes:
- `epicContext` — lean: epic overview + issue titles only (~400 tokens) — used by C1, C2, and strategy calls
- `fullContext` — rich: epic overview + per-issue descriptions up to 500 chars — used by Call A and scenario batches

Also added explicit quality floors: `minimum 8 risks`, `minimum 8 assumptions`, `minimum 5 open questions`.

**Final architecture:**

| Call | Sections | Context | Parallel? |
|---|---|---|---|
| A | 1–7 | `fullContext` | Yes |
| B1…Bn | 8 (batched, 12 issues/batch) | `epicContext` + batch detail | Yes |
| C1 | 9–16 | `epicContext` | Yes |
| C2 | 17–22 | `epicContext` | Yes |

All calls run via `Promise.all()`. Combined output: ~40 000–65 000 tokens.

### Result
All 22 sections generated with comprehensive content. Coverage scales to 50+ child issues.

### Errors / Blockers
- Leftover `BATCH_SIZE_STR` variable introduced during edits — cleaned up

---

## 2026-06-18 — Phase S (Stylize) — Test Strategy Feature Added

### Context
User provided a sample **Test Strategy for Ecommerce Website** (`.docx`). The sample had 8 sections: Objective, Scope, Focus Areas, Approach, Deliverables, Team & Schedule, Entry/Exit Criteria, Risks. Task: model a Test Strategy skill after this sample and integrate it into the React app.

### Done

**1. Created `tools/TestStrategy-Skill.md`**
- 200-line Claude system prompt for generating a 13-section test strategy document
- Expanded on the ecommerce sample: added Test Techniques table, Tooling & Automation, Assumptions, Open Questions sections
- Structured to produce a concise, decision-oriented strategy document (not a scenario-level plan)
- Minimum quality floors: 6 risks, 6 assumptions, 4 open questions

**2. Updated `server.js`**
- Loaded `TestStrategy-Skill.md` at startup as `TEST_STRATEGY_SKILL`
- Added `POST /api/generate-test-strategy` endpoint:
  - Single Claude call (`claude-sonnet-4-6`, `max_tokens: 8192`)
  - Uses `epicContext` only (lean — strategy is high-level, doesn't need per-issue descriptions)
  - Returns `{ jira_id, generated_at, test_strategy_content }`

**3. Created `src/components/OutputPanel.jsx`**
- Replaces the old `TestPlan.jsx` component
- Shows a **tab bar** (`Test Plan` | `Test Strategy`) when both documents are generated
- Falls back to single-doc view (no tabs) when only one document exists
- Each tab has its own "Download .md" button
- Tab selection is preserved when switching; auto-switches to whichever tab was generated last

**4. Updated `src/App.jsx`**
- Added `testStrategy` and `strategyStatus` state
- Added `generateTestStrategy()` async function
- Added **"Generate Test Strategy"** button (teal, disabled while any generation is in flight)
- Both generation buttons are mutually exclusive during generation (`isBusy` guard)
- Shared `downloadMarkdown(content, filename)` helper replaces old inline download
- Switched from `TestPlan` component to `OutputPanel`

**5. Updated `src/App.css`**
- `.btn-strategy` — teal button (`#0e7490`)
- `.btn-download-strategy` — teal download button for strategy tab
- `.status-bar.strategy` — cyan spinner text for strategy generation in progress
- `.tab-bar`, `.tab-btn`, `.tab-btn--active` — tab bar styles (GitHub-style active highlight)

### Result
App now generates two independent documents from the same JIRA Epic:
- **Test Plan** (22 sections, parallel multi-call, ~40k–65k tokens, 60–120 sec)
- **Test Strategy** (13 sections, single call, ~2k–3k tokens, 20–40 sec)
Both appear in a tabbed output panel with individual download buttons.

### Errors / Blockers
- None

---

## Phase T (Trigger) — Pending

### Not Yet Started
- Cloud hosting not configured
- No cron/webhook triggers set up
- Local `.env` holds all secrets

### Planned
- [ ] Deploy to cloud (Vercel / Railway / Render)
- [ ] Move secrets to cloud environment variables
- [ ] Optional: write generated `.md` files to `OUTPUT_DIR` on server
- [ ] Optional: configurable `BATCH_SIZE` via `.env`
