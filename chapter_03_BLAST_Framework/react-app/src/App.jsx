import { useState } from 'react';
import EpicDetails from './components/EpicDetails.jsx';
import OutputPanel from './components/OutputPanel.jsx';

export default function App() {
  const [epicId, setEpicId] = useState('SCRUM-6');
  const [epic, setEpic] = useState(null);
  const [testPlan, setTestPlan] = useState(null);
  const [testStrategy, setTestStrategy] = useState(null);
  const [fetchStatus, setFetchStatus] = useState('idle');
  const [generateStatus, setGenerateStatus] = useState('idle');
  const [strategyStatus, setStrategyStatus] = useState('idle');
  const [error, setError] = useState(null);

  async function fetchEpic() {
    setFetchStatus('loading');
    setEpic(null);
    setTestPlan(null);
    setTestStrategy(null);
    setGenerateStatus('idle');
    setStrategyStatus('idle');
    setError(null);
    try {
      const res = await fetch(`/api/epic/${epicId.trim()}`);
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to fetch epic');
      setEpic(data);
      setFetchStatus('done');
    } catch (err) {
      setError(err.message);
      setFetchStatus('error');
    }
  }

  async function generateTestPlan() {
    setGenerateStatus('loading');
    setTestPlan(null);
    setError(null);
    try {
      const res = await fetch('/api/generate-test-plan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ epic }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to generate test plan');
      setTestPlan(data);
      setGenerateStatus('done');
    } catch (err) {
      setError(err.message);
      setGenerateStatus('error');
    }
  }

  async function generateTestStrategy() {
    setStrategyStatus('loading');
    setTestStrategy(null);
    setError(null);
    try {
      const res = await fetch('/api/generate-test-strategy', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ epic }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Failed to generate test strategy');
      setTestStrategy(data);
      setStrategyStatus('done');
    } catch (err) {
      setError(err.message);
      setStrategyStatus('error');
    }
  }

  function downloadMarkdown(content, filename) {
    const blob = new Blob([content], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  const hasOutput = testPlan || testStrategy;
  const isBusy = generateStatus === 'loading' || strategyStatus === 'loading';

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-left">
          <span className="header-icon">🚀</span>
          <div>
            <h1>B.L.A.S.T. Test Plan Generator</h1>
            <p>Blueprint · Link · Architect · Stylize · Trigger</p>
          </div>
        </div>
        <div className="header-controls">
          <input
            className="epic-input"
            type="text"
            value={epicId}
            onChange={(e) => setEpicId(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && fetchEpic()}
            placeholder="JIRA Epic ID (e.g. SCRUM-6)"
          />
          <button
            className="btn btn-primary"
            onClick={fetchEpic}
            disabled={fetchStatus === 'loading' || !epicId.trim()}
          >
            {fetchStatus === 'loading' ? 'Fetching...' : 'Fetch Epic'}
          </button>
          <button
            className="btn btn-generate"
            onClick={generateTestPlan}
            disabled={!epic || isBusy}
          >
            {generateStatus === 'loading' ? 'Generating Plan...' : 'Generate Test Plan'}
          </button>
          <button
            className="btn btn-strategy"
            onClick={generateTestStrategy}
            disabled={!epic || isBusy}
          >
            {strategyStatus === 'loading' ? 'Generating Strategy...' : 'Generate Test Strategy'}
          </button>
        </div>
      </header>

      {error && (
        <div className="error-banner">
          <strong>Error:</strong> {error}
        </div>
      )}

      {fetchStatus === 'loading' && (
        <div className="status-bar">
          <span className="spinner" /> Connecting to JIRA and fetching {epicId}...
        </div>
      )}

      {generateStatus === 'loading' && (
        <div className="status-bar generating">
          <span className="spinner" /> Claude is generating the full test plan (parallel calls for all issues) — this may take 60–120 seconds...
        </div>
      )}

      {strategyStatus === 'loading' && (
        <div className="status-bar strategy">
          <span className="spinner" /> Claude is generating the test strategy — this may take 20–40 seconds...
        </div>
      )}

      <main className="app-body">
        {!epic && fetchStatus !== 'loading' && fetchStatus !== 'error' && (
          <div className="empty-state">
            <div className="empty-icon">📋</div>
            <h2>Enter a JIRA Epic ID and click Fetch Epic</h2>
            <p>Pre-filled with <strong>SCRUM-6</strong> — just click the button to start.</p>
          </div>
        )}

        {epic && (
          <div className={`panels ${hasOutput ? 'split' : 'single'}`}>
            <EpicDetails epic={epic} />
            {hasOutput && (
              <OutputPanel
                testPlan={testPlan}
                testStrategy={testStrategy}
                onDownloadPlan={() =>
                  downloadMarkdown(testPlan.test_plan_content, `${testPlan.jira_id}_test_plan.md`)
                }
                onDownloadStrategy={() =>
                  downloadMarkdown(testStrategy.test_strategy_content, `${testStrategy.jira_id}_test_strategy.md`)
                }
              />
            )}
          </div>
        )}
      </main>
    </div>
  );
}
