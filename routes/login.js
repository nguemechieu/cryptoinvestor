const express = require('express');
const router = express.Router();
const loginController = require('../controllers/loginController');

router.post('/auth/login',loginController.login);

module.exports = router;