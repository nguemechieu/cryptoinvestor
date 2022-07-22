const express = require('express');
const router = express.Router()

router.get('/register',(req,res,next) => {

    res.render('register', { title : 'Registration Form '})
    next();

});

module.exports = router;