const express = require('express');

const router = express.Router();


router.route('/').get((req, res) => {

    res.render('index', { title : 'Welcome to CryptoInvestor \n'+Date.now()});
});
router.route('/register').post((req, res) => {

    res.render('register', { title : 'User Registration'})

});

module.exports = router;