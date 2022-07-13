const express = require('express');
const router = express.Router();
const refreshTokenController = require('../controllers/refreshTokenController');

router.route("/refresh").get( refreshTokenController.handleRefreshToken);

module.exports = router;