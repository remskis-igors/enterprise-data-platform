const express = require('express');
const app = express();
const port = 3001;

// Base check endpoint
app.get('/api/status', (req, res) => {
  res.json({ status: "Dashboard backend is running", timestamp: new Date().toISOString() });
});

app.listen(port, () => {
  console.log(`Dashboard backend listening at http://localhost:${port}`);
});
