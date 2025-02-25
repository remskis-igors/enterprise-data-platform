import React, { useEffect, useState } from 'react';
import StatusWidget from './components/StatusWidget';

function App() {
  const [status, setStatus] = useState(null);

  useEffect(() => {
    fetch('/api/status')
      .then(res => res.json())
      .then(data => setStatus(data))
      .catch(err => console.error("Error fetching status:", err));
  }, []);

  return (
    <div>
      <h1>Dashboard</h1>
      {status ? <StatusWidget status={status} /> : <p>Loading...</p>}
    </div>
  );
}

export default App;
