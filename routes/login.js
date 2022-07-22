const express = require('express');
const router = express.Router();
const loginController = require('../controllers/loginController');

router.route('/auth/login').post (loginController.login);

module.exports = router;