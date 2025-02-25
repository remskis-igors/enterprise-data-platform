import React from 'react';

const StatusWidget = ({ status }) => (
  <div>
    <h2>Status</h2>
    <p>{status.status}</p>
    <small>{status.timestamp}</small>
  </div>
);

export default StatusWidget;
