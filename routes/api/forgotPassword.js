const express = require("express");
const router = express.Router();

const auth= require('../auth')
router.route('/#',auth,function (req,res,next){

    res.send("New password: " + req.body.password +"has been send to your email address\r\n");

    next();





});


module.exports = router;