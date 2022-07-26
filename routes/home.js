
const express = require('express');
const verifyJWT = require("../middleware/verifyJWT");
const router = express.Router();

router.get('/api/users/home',verifyJWT,(req, res) => {

    res.render('home', { title  : 'Home' });
});

module.exports = router;