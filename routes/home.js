
const express = require('express');
const router = express.Router();

router.get('/auth/home',(req, res) => {

    res.render('home', { title  : 'Home' });
});

module.exports = router;