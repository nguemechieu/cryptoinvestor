const nodemailer = require("nodemailer");
const express = require("express");
let router = express.Router();
const db = require("../_helpers/db");


router.post('/api/users/recover/account',(req, res) => {

  let emails = req.body.email;

  let found= db.User.findOne({where:{ email: emails }});

if (!found) {
    res.status(404).send({message   : "Email not found"})


}else {


    let transporter = nodemailer.createTransport({
        service: "live.com",
        auth: {
            user: "nguemechieu",
            pass: "Bigboss307##",
        },
    })


    let mailOptions = {
        from: "nguemechieu@live.com",
        to: req.body.email,
        subject: "Sending Email using CryptoInvestor",
        text: "resetPassword",
    }

    transporter.sendMail(mailOptions, function (error, info) {
        if (error) {
            console.log(error)
        } else {
            console.log("Sent: " + info.response)
        }
    });
}})
module.exports = router;