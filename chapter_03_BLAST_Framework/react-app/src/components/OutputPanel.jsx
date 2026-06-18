import { useState, useEffect, useRef } from 'react';
import { marked } from 'marked';

marked.setOptions({ breaks: true, gfm: true });

function MarkdownView({ content }) {
  const ref = useRef(null);
  useEffect(() => {
    if (ref.current) ref.current.innerHTML = marked.parse(content);
  }, [content]);
  return <div className="markdown-body" ref={ref} />;
}

export default function OutputPanel({ testPlan, testStrategy, onDownloadPlan, onDownloadStrategy }) {
  const hasPlan     = Boolean(testPlan);
  const hasStrategy = Boolean(testStrategy);

  // default to whichever tab appeared first; switch automatically when other is generated
  const [activeTab, setActiveTab] = useState(hasPlan ? 'plan' : 'strategy');

  useEffect(() => {
    if (hasStrategy && !hasPlan) setActiveTab('strategy');
    if (hasPlan && !hasStrategy) setActiveTab('plan');
  }, [hasPlan, hasStrategy]);

  // if only one doc exists, skip the tab bar
  const showTabs = hasPlan && hasStrategy;

  const activePlan     = activeTab === 'plan' && hasPlan;
  const activeStrategy = activeTab === 'strategy' && hasStrategy;

  const generatedAt = (iso) => new Date(iso).toLocaleString();

  return (
    <div className="panel test-plan-panel">
      <div className="panel-header">
        {showTabs ? (
          <div className="tab-bar">
            <button
              className={`tab-btn${activeTab === 'plan' ? ' tab-btn--active' : ''}`}
              onClick={() => setActiveTab('plan')}
            >
              Test Plan
            </button>
            <button
              className={`tab-btn${activeTab === 'strategy' ? ' tab-btn--active' : ''}`}
              onClick={() => setActiveTab('strategy')}
            >
              Test Strategy
            </button>
          </div>
        ) : (
          <h2>{hasPlan ? 'Generated Test Plan' : 'Generated Test Strategy'}</h2>
        )}

        <div className="panel-header-right">
          {activePlan && (
            <>
              <span className="generated-at">Generated {generatedAt(testPlan.generated_at)}</span>
              <button className="btn btn-download-sm" onClick={onDownloadPlan}>
                Download .md
              </button>
            </>
          )}
          {activeStrategy && (
            <>
              <span className="generated-at">Generated {generatedAt(testStrategy.generated_at)}</span>
              <button className="btn btn-download-sm btn-download-strategy" onClick={onDownloadStrategy}>
                Download .md
              </button>
            </>
          )}
        </div>
      </div>

      {activePlan     && <MarkdownView content={testPlan.test_plan_content} />}
      {activeStrategy && <MarkdownView content={testStrategy.test_strategy_content} />}
    </div>
  );
}
