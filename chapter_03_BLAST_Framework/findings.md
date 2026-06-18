# Findings — JIRA Test Plan & Strategy Generator

> Maintained per B.L.A.S.T. Protocol: store every meaningful discovery here so it is never re-derived.

---

## Phase B — Blueprint

### Discovery Answers (confirmed with user)
| Question | Answer |
|---|---|
| North Star | Generate a professional, execution-ready Test Plan from any JIRA Epic in one click |
| Integrations | JIRA Cloud REST API v3 + Anthropic Claude API (`claude-sonnet-4-6`) |
| Source of Truth | JIRA Epic + its child issues (fetched live) |
| Delivery Payload | Rendered Markdown in-browser + downloadable `.md` file |
| Behavioral Rules | Use `TestPlan-Skill.md` and `TestStrategy-Skill.md` as Claude system prompts; never invent JIRA data |

### Skills / System Prompts (Layer 1 Architecture)
- `tools/TestPlan-Skill.md` — 307-line system prompt defining a 22-section enterprise test plan. Loaded at server startup as `TEST_PLAN_SKILL`.
- `tools/TestStrategy-Skill.md` — 200-line system prompt defining a 13-section test strategy (modeled on the ecommerce test strategy sample). Loaded at server startup as `TEST_STRATEGY_SKILL`.
- Both are read via `readFileSync` at server boot — zero runtime I/O overhead per request.

---

## Phase L — Link

### JIRA REST API (verified)
- Base URL: `https://souravdm.atlassian.net`
- Auth: `Authorization: Basic base64(email:apiToken)` — JIRA Cloud token, not account password
- Epic fetch: `GET /rest/api/3/issue/{epicId}`
- Child issues: `GET /rest/api/3/search?jql=parent={epicId}&maxResults=50&fields=summary,status,...`
- Description format: **Atlassian Document Format (ADF)** — a JSON node tree; requires recursive parsing
- ADF node types handled: `doc`, `paragraph`, `heading`, `text`, `hardBreak`, `bulletList`, `orderedList`, `listItem`, `codeBlock`, `blockquote`; unknown nodes with `.content` are recursed transparently

### Anthropic Claude API (verified)
- SDK: `@anthropic-ai/sdk` (Node.js)
- Model: `claude-sonnet-4-6`
- Max output tokens per call: **8 192** (hard cap without extended-output beta)
- Max context window: 200K tokens — sufficient for any realistic Epic
- Claude Pro (claude.ai subscription) does **not** include API access — API requires separate credit at `console.anthropic.com`
- API key format: `sk-ant-api03-...` — obtained from console.anthropic.com → Settings → API Keys

### .env location
- File: `chapter_03_BLAST_Framework/.env` (parent of `react-app/`)
- Loaded via `config({ path: join(__dirname, '../.env') })` — not global dotenv import

---

## Phase A — Architect

### App Architecture
- **Frontend**: React 18 + Vite (dev server port 5173)
- **Backend**: Express.js API (port 3001)
- **Proxy**: Vite dev proxy `/api` → `http://localhost:3001` — eliminates CORS issues in development
- **Startup**: `concurrently "node server.js" "vite"` via `npm run dev`
- **ES Module `__dirname`**: `const __dirname = dirname(fileURLToPath(import.meta.url))` — required because `"type": "module"` in package.json

### Token Budget Problem & Multi-Call Architecture (Test Plan)
A single call with `max_tokens: 8192` cannot fit a 22-section plan AND test scenarios for 17+ issues.

**Solution — parallel specialized calls via `Promise.all()`:**

| Call | Sections | Context Used | Why |
|---|---|---|---|
| A — Structure | 1–7 (Doc Control → Test Design Techniques) | `fullContext` (epic + all child descriptions) | Needs per-issue detail to populate items table |
| B1…Bn — Scenarios | 8 per batch (12 issues / batch) | `epicContext` + batch child details | Each batch gets its own 8 192-token budget |
| C1 — Process | 9–16 (Data → Automation Strategy) | `epicContext` (lean, titles only) | These sections don't need per-issue descriptions |
| C2 — Risk & Sign-off | 17–22 (Non-Functional → Sign-Off) | `epicContext` (lean, titles only) | Same — input savings maximise output budget |

- Wall-clock time ≈ single call (all run in parallel)
- For 50 issues: `ceil(50/12) = 5` scenario batches → **8 total Claude calls → ~65 000 combined output tokens**

### Two Context Sizes
| Context | Contents | ~Input Tokens | Used By |
|---|---|---|---|
| `epicContext` | Epic summary + issue titles only | ~400 | Tail calls C1, C2; strategy call |
| `fullContext` | `epicContext` + per-issue descriptions (up to 500 chars each) | ~2 000–5 000 | Call A (structure), scenario batches |

**Finding**: Sending full child descriptions to tail calls wastes ~2 000 input tokens, reducing available output budget. Lean context for tail calls consistently produced more comprehensive sections 17–22.

### Explicit Quality Floors (Prompts)
Adding minimum counts in prompts prevents one-line stubs:
- Section 18: `minimum 8 risks`
- Section 19: `minimum 8 assumptions`
- Section 21: `minimum 5 open questions`
- Strategy section 11: `minimum 6 risks`
- Strategy section 12: `minimum 6 assumptions`

### Test Strategy Architecture (single call)
- The strategy is a concise, decision-oriented document (~2 000–3 000 output tokens)
- Single Claude call with `max_tokens: 8192` is sufficient
- Uses `epicContext` only (issue titles, no descriptions) — strategy is high-level by design
- Endpoint: `POST /api/generate-test-strategy` → returns `{ jira_id, generated_at, test_strategy_content }`

---

## Phase S — Stylize

### React App UI
- Dark theme (#0f1117 background, GitHub-style colour palette)
- Sticky header with Epic ID input, Fetch Epic, Generate Test Plan, Generate Test Strategy buttons
- Left panel: Epic Details (badges for status/priority/type, child issue list with descriptions)
- Right panel: `OutputPanel` — tabbed view showing Test Plan or Test Strategy
- Tab bar appears only when both documents are generated; single-doc mode shows no tabs
- Markdown rendered via `marked.js` with styled `markdown-body` (tables, code blocks, headings all styled)
- Download as `.md` button per document tab

### Output Documents
| Document | Endpoint | Sections | Output Size |
|---|---|---|---|
| Test Plan | `POST /api/generate-test-plan` | 22 (sections 1–22) | ~40 000–65 000 tokens across parallel calls |
| Test Strategy | `POST /api/generate-test-strategy` | 13 (Objective → Open Questions) | ~2 000–3 000 tokens in one call |

### Test Strategy Section Map (based on ecommerce sample)
| # | Section | Key Content |
|---|---|---|
| 1 | Objective | Quality goals, business risk reduction, ship criteria |
| 2 | Scope | In Scope (child issues by ID) / Out of Scope |
| 3 | Focus Areas | Quality dimensions relevant to this Epic |
| 4 | Test Approach | Testing types, black/white box, manual vs automated balance |
| 5 | Test Techniques | Equivalence partitioning, BVA, decision table, state transition (table) |
| 6 | Tooling & Automation | Framework, CI/CD integration, what to automate vs manual |
| 7 | Deliverables | Table with owner and timing placeholders |
| 8 | Team & Schedule | Headcount + phased schedule table |
| 9 | Entry Criteria | Pre-test readiness checklist |
| 10 | Exit Criteria | Completion definition |
| 11 | Risks & Mitigations | Table — min 6 risks |
| 12 | Assumptions | Numbered list — min 6 |
| 13 | Open Questions | Min 4 questions for stakeholders |

---

## Phase T — Trigger

### Status: Local only (development)
- App runs via `npm run dev` in `chapter_03_BLAST_Framework/react-app/`
- No cloud deployment configured yet

### Pending for Trigger phase
- [ ] Cloud hosting (Vercel / Railway / Render)
- [ ] Environment variables in cloud secrets
- [ ] Optional: save generated `.md` files to server `OUTPUT_DIR`
- [ ] Optional: configurable `BATCH_SIZE` via `.env`

---

## Bugs Resolved

| Date | Bug | Root Cause | Fix |
|---|---|---|---|
| 2026-06-16 | `Failed to resolve import "react/jsx-dev-runtime"` | `react` and `react-dom` missing from `package.json` | Added both to `dependencies`, ran `npm install` |
| 2026-06-16 | Only 2 of 17 features in test plan | Single 8 192-token call exhausted before reaching all issues | Parallel call architecture (A + B-batches + C1 + C2) |
| 2026-06-16 | Sections 17–22 not generated | Single tail call (sections 9–22) hit token limit | Split into C1 (9–16) and C2 (17–22) as separate parallel calls |
| 2026-06-16 | Test plan content shorter after C1/C2 split | Tail calls receiving full child descriptions as input, wasting ~2 000 input tokens | Introduced `epicContext` (lean) for all tail calls |
| 2026-06-16 | `BATCH_SIZE_STR` leftover variable | Accidental name during edit | Cleaned up |
