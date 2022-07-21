const express = require('express');
const router = express.Router();
const registerController = require('../controllers/registerController');
const path = require("path");

router.post('/auth/register', registerController.handleNewUser);

router.post('/register',(req, res) => {

    res.sendFile(path.join(__dirname, '..', 'views', 'register.html'));

});
module.exports = router;