const express = require('express');
const router = express.Router();
const loginController = require('../controllers/loginController');

router.post('/api/users/login/now',loginController.loginController);

module.exports = router;