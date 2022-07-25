const express = require('express');
const router = express.Router();
const registerController = require('../controllers/registerController');


router.post('/api/users/signup/now', registerController.signup);



module.exports = router;