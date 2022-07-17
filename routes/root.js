const express = require('express');
const router = express.Router();
const path = require('path');
router.route('^/$|/index(.html)?').get( (req, res) => {
    res.sendFile(path.join(__dirname, '..', 'public/views', 'index.html'));
});
router.route('/register').get( (req, res) => {

    res.sendFile(path.join(__dirname, 'public/views', 'register.html'));
});

module.exports = router;