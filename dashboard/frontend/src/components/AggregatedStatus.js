import React, { useEffect, useState } from 'react';

const AggregatedStatus = () => {
  const [data, setData] = useState(null);

  useEffect(() => {
    fetch('http://localhost:5000/api/aggregated')
      .then((res) => res.json())
      .then((data) => setData(data))
      .catch((err) => console.error('Error fetching aggregated data:', err));
  }, []);

  if (!data) {
    return <div>Loading aggregated data...</div>;
  }

  return (
    <div style={{ border: '1px solid #ccc', padding: '1rem', borderRadius: '8px', margin: '1rem' }}>
      <h2>Aggregated Data</h2>
      <p><strong>Count:</strong> {data.count}</p>
      <p><strong>Sum:</strong> {data.sum}</p>
      <p><strong>Average:</strong> {data.average.toFixed(2)}</p>
      <p><strong>Last Update:</strong> {data.last_update}</p>
    </div>
  );
};

export default AggregatedStatus;
