const express = require('express');
const router = express.Router();
const registerController = require('../controllers/registerController');


router.route('/auth/register').post(registerController.signup);



module.exports = router;