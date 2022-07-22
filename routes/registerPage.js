const express = require('express');
const router = express.Router()

router.route('/register').post((req,res,next) => {

    res.render('register', { title : 'Registration Form '})
    next();

});

module.exports = router;