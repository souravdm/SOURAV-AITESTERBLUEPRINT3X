const priorityColor = {
  Highest: '#d32f2f',
  High: '#f57c00',
  Medium: '#fbc02d',
  Low: '#388e3c',
  Lowest: '#1976d2',
};

const statusColor = {
  'To Do': '#546e7a',
  'In Progress': '#1565c0',
  Done: '#2e7d32',
  Resolved: '#2e7d32',
};

function Badge({ text, colorMap, fallback = '#607d8b' }) {
  const color = colorMap?.[text] || fallback;
  return (
    <span className="badge" style={{ backgroundColor: color }}>
      {text}
    </span>
  );
}

function ChildRow({ child }) {
  return (
    <div className="child-row">
      <div className="child-header">
        <span className="child-id">{child.id}</span>
        <Badge text={child.status} colorMap={statusColor} />
        <Badge text={child.issuetype} fallback="#5c6bc0" />
        {child.priority && (
          <Badge text={child.priority} colorMap={priorityColor} />
        )}
      </div>
      <div className="child-summary">{child.summary}</div>
      {child.description && (
        <div className="child-desc">{child.description.slice(0, 200)}{child.description.length > 200 ? '…' : ''}</div>
      )}
      {child.assignee && <div className="child-meta">Assignee: {child.assignee}</div>}
    </div>
  );
}

export default function EpicDetails({ epic }) {
  return (
    <div className="panel epic-panel">
      <div className="panel-header">
        <h2>JIRA Epic</h2>
        <span className="epic-id-chip">{epic.id}</span>
      </div>

      <div className="epic-meta-row">
        <Badge text={epic.status} colorMap={statusColor} />
        <Badge text={epic.issuetype} fallback="#5c6bc0" />
        {epic.priority && <Badge text={epic.priority} colorMap={priorityColor} />}
      </div>

      <h3 className="epic-summary">{epic.summary}</h3>

      {epic.description && (
        <div className="section">
          <div className="section-label">Description</div>
          <p className="epic-description">{epic.description}</p>
        </div>
      )}

      <div className="meta-grid">
        {epic.reporter && (
          <div className="meta-item">
            <span className="meta-label">Reporter</span>
            <span>{epic.reporter}</span>
          </div>
        )}
        {epic.assignee && (
          <div className="meta-item">
            <span className="meta-label">Assignee</span>
            <span>{epic.assignee}</span>
          </div>
        )}
        {epic.labels?.length > 0 && (
          <div className="meta-item">
            <span className="meta-label">Labels</span>
            <span>{epic.labels.join(', ')}</span>
          </div>
        )}
      </div>

      <div className="section">
        <div className="section-label">
          Child Issues
          <span className="count-chip">{epic.children?.length || 0}</span>
        </div>
        {epic.children?.length === 0 && (
          <p className="empty-children">No child issues found for this Epic.</p>
        )}
        <div className="children-list">
          {epic.children?.map((child) => (
            <ChildRow key={child.id} child={child} />
          ))}
        </div>
      </div>
    </div>
  );
}
