import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { readFileSync } from 'fs';
import { config } from 'dotenv';
import express from 'express';
import cors from 'cors';
import Anthropic from '@anthropic-ai/sdk';

const __dirname = dirname(fileURLToPath(import.meta.url));

// Load .env from parent directory (chapter_03_BLAST_Framework/.env)
config({ path: join(__dirname, '../.env') });

// Load skill files as Claude system prompts
const TEST_PLAN_SKILL     = readFileSync(join(__dirname, '../tools/TestPlan-Skill.md'), 'utf-8');
const TEST_STRATEGY_SKILL = readFileSync(join(__dirname, '../tools/TestStrategy-Skill.md'), 'utf-8');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.API_PORT || 3001;

const JIRA_BASE_URL = process.env.JIRA_BASE_URL;
const JIRA_EMAIL = process.env.JIRA_EMAIL;
const JIRA_API_TOKEN = process.env.JIRA_API_TOKEN;
const ANTHROPIC_API_KEY = process.env.ANTHROPIC_API_KEY;

const jiraAuthHeader = () =>
  'Basic ' + Buffer.from(`${JIRA_EMAIL}:${JIRA_API_TOKEN}`).toString('base64');

// ADF (Atlassian Document Format) → plain text
function adfToText(node) {
  if (!node) return '';
  if (typeof node === 'string') return node;
  if (node.type === 'text') return node.text || '';
  if (node.type === 'hardBreak') return '\n';
  if (node.type === 'paragraph') return (node.content || []).map(adfToText).join('') + '\n';
  if (node.type === 'heading') return (node.content || []).map(adfToText).join('') + '\n';
  if (node.type === 'bulletList') return (node.content || []).map(adfToText).join('');
  if (node.type === 'orderedList') return (node.content || []).map(adfToText).join('');
  if (node.type === 'listItem') return '- ' + (node.content || []).map(adfToText).join('').trim() + '\n';
  if (node.type === 'codeBlock') return '```\n' + (node.content || []).map(adfToText).join('') + '\n```\n';
  if (node.type === 'blockquote') return (node.content || []).map(adfToText).join('');
  if (node.type === 'doc') return (node.content || []).map(adfToText).join('');
  if (node.content) return node.content.map(adfToText).join('');
  return '';
}

function parseDescription(description) {
  if (!description) return '';
  if (typeof description === 'string') return description;
  // ADF format
  return adfToText(description).trim();
}

// GET /api/epic/:epicId  — fetch epic + child issues
app.get('/api/epic/:epicId', async (req, res) => {
  const { epicId } = req.params;

  if (!JIRA_BASE_URL || !JIRA_EMAIL || !JIRA_API_TOKEN) {
    return res.status(500).json({ error: 'JIRA credentials not configured in .env' });
  }

  try {
    const headers = {
      Authorization: jiraAuthHeader(),
      Accept: 'application/json',
    };

    // Fetch the epic itself
    const epicResp = await fetch(`${JIRA_BASE_URL}/rest/api/3/issue/${epicId}`, { headers });
    if (!epicResp.ok) {
      const text = await epicResp.text();
      return res.status(epicResp.status).json({ error: `JIRA API error: ${epicResp.status}`, detail: text });
    }
    const epicData = await epicResp.json();

    // Fetch child issues — try parent= first, fall back to "Epic Link"=
    const jql = encodeURIComponent(`parent = ${epicId} ORDER BY created ASC`);
    const childResp = await fetch(
      `${JIRA_BASE_URL}/rest/api/3/search?jql=${jql}&maxResults=50&fields=summary,status,issuetype,priority,description,assignee,labels,subtasks`,
      { headers }
    );
    let children = [];
    if (childResp.ok) {
      const childData = await childResp.json();
      children = (childData.issues || []).map((issue) => ({
        id: issue.key,
        summary: issue.fields.summary,
        status: issue.fields.status?.name,
        issuetype: issue.fields.issuetype?.name,
        priority: issue.fields.priority?.name,
        description: parseDescription(issue.fields.description),
        assignee: issue.fields.assignee?.displayName || null,
        labels: issue.fields.labels || [],
      }));
    }

    const epic = {
      id: epicData.key,
      summary: epicData.fields.summary,
      description: parseDescription(epicData.fields.description),
      status: epicData.fields.status?.name,
      priority: epicData.fields.priority?.name,
      issuetype: epicData.fields.issuetype?.name,
      labels: epicData.fields.labels || [],
      assignee: epicData.fields.assignee?.displayName || null,
      reporter: epicData.fields.reporter?.displayName || null,
      children,
    };

    res.json(epic);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// POST /api/generate-test-plan  — send epic data to Claude
app.post('/api/generate-test-plan', async (req, res) => {
  const { epic } = req.body;
  if (!epic) return res.status(400).json({ error: 'Missing epic data' });

  const apiKey = ANTHROPIC_API_KEY;
  if (!apiKey || apiKey === 'your-claude-api-key') {
    return res.status(500).json({ error: 'ANTHROPIC_API_KEY not set in .env' });
  }

  const client = new Anthropic({ apiKey });

  const children = epic.children || [];
  const today = new Date().toISOString().split('T')[0];

  // ── Two context sizes ─────────────────────────────────────────────────────
  // epicContext  — lean, used by tail calls (no per-issue descriptions)
  // fullContext  — rich, used by structure + scenario batches
  const epicContext = `
EPIC ID: ${epic.id}
Summary: ${epic.summary}
Status: ${epic.status}
Priority: ${epic.priority || 'N/A'}
Issue Type: ${epic.issuetype}
Labels: ${epic.labels.join(', ') || 'None'}
Assignee: ${epic.assignee || 'Unassigned'}
Reporter: ${epic.reporter || 'N/A'}
Today's date: ${today}

EPIC DESCRIPTION:
${epic.description || 'No description provided.'}

CHILD ISSUE TITLES (${children.length} total):
${children.map((c) => `- [${c.id}] ${c.summary} (${c.issuetype}, ${c.status})`).join('\n') || 'None'}
`.trim();

  const fullChildSummary = children
    .map(
      (c, i) =>
        `${i + 1}. [${c.id}] ${c.summary} (${c.issuetype}, ${c.status}, Priority: ${c.priority || 'N/A'})` +
        (c.description ? `\n   Description: ${c.description.slice(0, 500)}` : '') +
        (c.assignee ? `\n   Assignee: ${c.assignee}` : '') +
        (c.labels?.length ? `\n   Labels: ${c.labels.join(', ')}` : '')
    )
    .join('\n\n');

  const fullContext = `${epicContext}

FULL CHILD ISSUE DETAILS:
${fullChildSummary || 'No child issues found.'}`;

  const BATCH_SIZE = 12;

  // Split children into batches
  const batches = [];
  for (let i = 0; i < Math.max(children.length, 1); i += BATCH_SIZE) {
    batches.push(children.slice(i, i + BATCH_SIZE));
  }

  // ── Call A: Sections 1–7 (structural overview) ───────────────────────────
  const promptStructure = `${fullContext}

TASK: Generate ONLY Sections 1 through 7 of the professional test plan as defined in your instructions. Be comprehensive and detailed — this is an enterprise-grade test plan for stakeholder review.
Sections to produce:
  1. Document Control
  2. Objective
  3. Product / Requirement Summary
  4. Scope of Testing (4.1 In Scope, 4.2 Out of Scope)
  5. Test Items / Features to be Tested — include a row for EVERY child issue listed
  6. Test Approach / Test Strategy
  7. Test Design Techniques
Do NOT generate Section 8 or beyond. Stop after completing Section 7.`;

  // ── Calls B1…Bn: Section 8 batches (one call per batch) ──────────────────
  const scenarioPrompts = batches.map((batch, idx) => {
    const batchDetail = batch
      .map(
        (c, i) =>
          `${i + 1}. [${c.id}] ${c.summary}\n` +
          `   Type: ${c.issuetype} | Status: ${c.status} | Priority: ${c.priority || 'N/A'}\n` +
          (c.description ? `   Description: ${c.description.slice(0, 700)}\n` : '') +
          (c.assignee ? `   Assignee: ${c.assignee}\n` : '') +
          (c.labels?.length ? `   Labels: ${c.labels.join(', ')}\n` : '')
      )
      .join('\n');

    const startId = idx * BATCH_SIZE + 1;
    const endId = idx * BATCH_SIZE + batch.length;

    return `${epicContext}

TASK: Generate ONLY Section 8 (Test Scenarios) content for the ${batch.length} child issues in this batch.
Batch ${idx + 1} of ${batches.length} — issues ${startId}–${endId} of ${children.length} total.

Output format — start with this heading exactly:
### Batch ${idx + 1} — Issues ${startId}–${endId} (${batch.length} issues)

Then for EACH issue below generate:
1. A sub-heading: #### [ISSUE-ID] Issue Summary
2. A Markdown table with columns: Scenario ID | Scenario Description | Test Type | Priority | Expected Outcome
3. Minimum 4 rows per issue: 2 Positive, 1 Negative, 1 Boundary/Edge Case
4. Scenario IDs start at TS-${String(startId).padStart(3, '0')}

Use actual feature names and behaviors from the issue descriptions. Do NOT skip any issue.

ISSUES IN THIS BATCH:
${batchDetail}`;
  });

  // ── Call C1: Sections 9–16 (data, environments, criteria, defects, deliverables, execution, automation) ─
  const promptTail1 = `${epicContext}

TASK: Generate ONLY Sections 9 through 16 of the professional test plan as defined in your instructions. Be comprehensive — each section must be fully fleshed out, not a stub.

Start directly with "## 9. Test Data Strategy" and produce all 8 sections:
  9.  Test Data Strategy — valid/invalid/boundary/null data examples, sample payloads where relevant
  10. Environment Requirements — table of environments (Dev/QA/Staging/Prod), URLs, credentials strategy, tools
  11. Entry Criteria — minimum 6 criteria as a checklist
  12. Exit Criteria — minimum 6 criteria as a checklist
  13. Defect Management Process — lifecycle, severity/priority matrix, full defect template
  14. Test Deliverables — complete list with owner and due-date placeholders
  15. Test Execution Plan — table with Phase, Activity, Owner, Start, End, Dependency, Deliverable
  16. Automation Strategy — framework recommendation, what to automate vs manual, CI/CD integration

Do NOT generate Section 17 or beyond.`;

  // ── Call C2: Sections 17–22 (non-functional, risks, assumptions, dependencies, open questions, sign-off) ─
  const promptTail2 = `${epicContext}

TASK: Generate ONLY Sections 17 through 22 of the professional test plan as defined in your instructions. Be thorough — these sections are critical for enterprise stakeholder sign-off.

Start directly with "## 17. Non-Functional Testing Considerations" and produce all 6 sections:
  17. Non-Functional Testing Considerations — performance, load, security, reliability, observability; mark N/A only if clearly inapplicable
  18. Risks and Mitigations — full table (Risk ID | Description | Impact | Probability | Mitigation | Owner); minimum 8 risks covering environment, data, dependencies, schedule, and test coverage gaps
  19. Assumptions — minimum 8 specific, numbered assumptions derived from the Epic and its child issues
  20. Dependencies — list all requirement, environment, API, data, credential, and team dependencies
  21. Open Questions — minimum 5 specific questions that must be resolved before or during testing
  22. Approval / Sign-Off — table with Name, Role, Approval Status, Date, Comments

Do NOT regenerate sections 1–16.`;

  try {
    console.log(`Generating test plan: 1 structure + ${batches.length} scenario batch(es) + 2 tail calls (${children.length} issues total)`);

    const allPromises = [
      // A — sections 1–7
      client.messages.create({
        model: 'claude-sonnet-4-6',
        max_tokens: 8192,
        system: TEST_PLAN_SKILL,
        messages: [{ role: 'user', content: promptStructure }],
      }),
      // B1…Bn — section 8 scenario batches
      ...scenarioPrompts.map((prompt) =>
        client.messages.create({
          model: 'claude-sonnet-4-6',
          max_tokens: 8192,
          system: TEST_PLAN_SKILL,
          messages: [{ role: 'user', content: prompt }],
        })
      ),
      // C1 — sections 9–16
      client.messages.create({
        model: 'claude-sonnet-4-6',
        max_tokens: 8192,
        system: TEST_PLAN_SKILL,
        messages: [{ role: 'user', content: promptTail1 }],
      }),
      // C2 — sections 17–22
      client.messages.create({
        model: 'claude-sonnet-4-6',
        max_tokens: 8192,
        system: TEST_PLAN_SKILL,
        messages: [{ role: 'user', content: promptTail2 }],
      }),
    ];

    const results = await Promise.all(allPromises);

    const structureText       = results[0].content[0].text;
    const scenarioBatchTexts  = results.slice(1, 1 + batches.length).map((r) => r.content[0].text);
    const tail1Text           = results[1 + batches.length].content[0].text;
    const tail2Text           = results[2 + batches.length].content[0].text;

    const section8 =
      `## 8. Test Scenarios\n\n` +
      `> Generated across ${batches.length} batch(es) — ${children.length} issues total\n\n` +
      scenarioBatchTexts.join('\n\n---\n\n');

    const testPlanMarkdown = [structureText, section8, tail1Text, tail2Text].join('\n\n---\n\n');

    res.json({
      jira_id: epic.id,
      generated_at: new Date().toISOString(),
      test_plan_content: testPlanMarkdown,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// POST /api/generate-test-strategy  — send epic data to Claude for a high-level test strategy
app.post('/api/generate-test-strategy', async (req, res) => {
  const { epic } = req.body;
  if (!epic) return res.status(400).json({ error: 'Missing epic data' });

  const apiKey = ANTHROPIC_API_KEY;
  if (!apiKey || apiKey === 'your-claude-api-key') {
    return res.status(500).json({ error: 'ANTHROPIC_API_KEY not set in .env' });
  }

  const client = new Anthropic({ apiKey });

  const children = epic.children || [];
  const today = new Date().toISOString().split('T')[0];

  const epicContext = `
EPIC ID: ${epic.id}
Summary: ${epic.summary}
Status: ${epic.status}
Priority: ${epic.priority || 'N/A'}
Issue Type: ${epic.issuetype}
Labels: ${epic.labels.join(', ') || 'None'}
Assignee: ${epic.assignee || 'Unassigned'}
Reporter: ${epic.reporter || 'N/A'}
Today's date: ${today}

EPIC DESCRIPTION:
${epic.description || 'No description provided.'}

CHILD ISSUES (${children.length} total):
${children.map((c, i) => `${i + 1}. [${c.id}] ${c.summary} (${c.issuetype}, ${c.status}, Priority: ${c.priority || 'N/A'})`).join('\n') || 'None'}
`.trim();

  const prompt = `${epicContext}

TASK: Generate a professional, concise, and actionable Test Strategy document for this JIRA Epic as defined in your instructions.

The strategy must reference the specific Epic and child issue summaries — do not be generic. Use the child issue IDs and titles to make every section specific to this product area.

Produce all 13 sections as defined in your instructions:
1. Objective
2. Scope (2.1 In Scope, 2.2 Out of Scope)
3. Focus Areas
4. Test Approach
5. Test Techniques (table)
6. Test Tooling & Automation Strategy
7. Deliverables (table)
8. Team & Schedule (8.1 Team, 8.2 Proposed Schedule table)
9. Entry Criteria
10. Exit Criteria
11. Risks & Mitigations (table — minimum 6 risks)
12. Assumptions (minimum 6 numbered)
13. Open Questions (minimum 4)`;

  try {
    console.log(`Generating test strategy for ${epic.id} (${children.length} child issues)`);

    const result = await client.messages.create({
      model: 'claude-sonnet-4-6',
      max_tokens: 8192,
      system: TEST_STRATEGY_SKILL,
      messages: [{ role: 'user', content: prompt }],
    });

    res.json({
      jira_id: epic.id,
      generated_at: new Date().toISOString(),
      test_strategy_content: result.content[0].text,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`BLAST API server running on http://localhost:${PORT}`);
  console.log(`JIRA: ${JIRA_BASE_URL || 'NOT CONFIGURED'}`);
  console.log(`Claude: ${ANTHROPIC_API_KEY && ANTHROPIC_API_KEY !== 'your-claude-api-key' ? 'CONFIGURED' : 'NOT CONFIGURED'}`);
});
