import React from 'react';

const StatusWidget = ({ status }) => {
  if (!status) {
    return <div>Loading...</div>;
  }

  return (
    <div style={{
      border: '1px solid #ccc',
      borderRadius: '8px',
      padding: '1rem',
      maxWidth: '400px',
      margin: '1rem auto',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    }}>
      <h2>System Status</h2>
      <p><strong>Status:</strong> {status.status}</p>
      <p><strong>Timestamp:</strong> {new Date(status.timestamp).toLocaleString()}</p>
    </div>
  );
};

export default StatusWidget;
