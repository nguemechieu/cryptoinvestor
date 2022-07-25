const express = require('express');
const router = express.Router();
const logoutController = require('../controllers/logoutController');

router.get('/api/users/logout',logoutController.handleLogout);

module.exports = router;