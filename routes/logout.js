const express = require('express');
const router = express.Router();
const logoutController = require('../controllers/logoutController');

router.get('/logout', logoutController.handleLogout);

module.exports = router;