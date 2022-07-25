const express = require('express');
const router = express.Router();
const refreshTokenController = require('../controllers/refreshTokenController');

router.get('/api/users/refresh', refreshTokenController.handleRefreshToken);

module.exports = router;