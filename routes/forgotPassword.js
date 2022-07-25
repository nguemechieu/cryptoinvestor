
const  express = require("express");


const router = express.Router();

router.get('/api/users/forgot/password', (req, res) => {
    res.render('forgotPassword', { title    :"Recover your account"
    })
})
;
module.exports =router;