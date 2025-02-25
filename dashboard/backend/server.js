const express = require('express');
const path = require('path');
const app = express();
const port = 3001;

// Distribution of the assembled React application
app.use(express.static(path.join(__dirname, 'public')));

// Endpoint example /api/status
app.get('/api/status', (req, res) => {
  res.json({ status: "Dashboard backend is running", timestamp: new Date().toISOString() });
});

// for all others request return index.html
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(port, () => {
  console.log(`Dashboard backend listening at http://localhost:${port}`);
});
