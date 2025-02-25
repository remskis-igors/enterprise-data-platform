import React, { useEffect, useState } from 'react';
import StatusWidget from './components/StatusWidget';

function App() {
  const [status, setStatus] = useState(null);

  useEffect(() => {
    fetch('/api/status')
      .then(res => {
        if (!res.ok) {
          throw new Error('Network response was not ok');
        }
        return res.json();
      })
      .then(data => setStatus(data))
      .catch(err => console.error("Error fetching status:", err));
  }, []);

  return (
    <div>
      <h1>Dashboard</h1>
      <StatusWidget status={status} />
    </div>
  );
}

export default App;
