import { useEffect, useRef } from 'react';
import { marked } from 'marked';

marked.setOptions({ breaks: true, gfm: true });

export default function TestPlan({ testPlan, onDownload }) {
  const contentRef = useRef(null);

  useEffect(() => {
    if (contentRef.current) {
      contentRef.current.innerHTML = marked.parse(testPlan.test_plan_content);
    }
  }, [testPlan.test_plan_content]);

  const generatedAt = new Date(testPlan.generated_at).toLocaleString();

  return (
    <div className="panel test-plan-panel">
      <div className="panel-header">
        <h2>Generated Test Plan</h2>
        <div className="panel-header-right">
          <span className="generated-at">Generated {generatedAt}</span>
          <button className="btn btn-download-sm" onClick={onDownload}>
            Download .md
          </button>
        </div>
      </div>
      <div className="markdown-body" ref={contentRef} />
    </div>
  );
}
