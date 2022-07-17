const express = require("express");
const router = express.Router();


router.route('/forgotPassword').get((req,res,next)=>{

    res.send("New password: " + req.body.password +"has been send to your email address\r\n");

    next();

});


module.exports = router;