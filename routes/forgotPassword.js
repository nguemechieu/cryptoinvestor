const express = require('express');
const router = express.Router();

router.get('/forgotPassword',(req, res) => {
    let randomPassword = Math.random * 128;
    res.render('forgotPassword', {
        title: 'Forgot Password',
        randomPassword: " Here is your new password validation code .It will be available for 20 minutes only\nPassword code :" +
            randomPassword
    });
})

module.exports = router;