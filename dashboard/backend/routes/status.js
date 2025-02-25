const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    status: "Dashboard backend active",
    lastUpdate: new Date().toLocaleString()
  });
});

module.exports = router;
