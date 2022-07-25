const express = require('express');
const router = express.Router();
const loginController = require('../controllers/loginController');

router.post('/api/users/login',loginController.login);

module.exports = router;