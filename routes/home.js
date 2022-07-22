
const express = require('express');
const router = express.Router();

router.route('/auth/home').get((req, res) => {

    res.render('home', { title  : 'Home' });
});

module.exports = router;