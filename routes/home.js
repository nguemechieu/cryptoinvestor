
const express = require('express');
const router = express.Router();

router.get('/api/users/home',(req, res) => {

    res.render('home', { title  : 'Home' });
});

module.exports = router;