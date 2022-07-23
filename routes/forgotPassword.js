const nodemailer = require("nodemailer");
const db = require("../_helpers/db");
const  express = require("express");
const req = require("express/lib/request");

const router = express.Router();

let transporter = nodemailer.createTransport({
    service: "live",
    auth: {
        user: "nguemechieu@live.com",
        pass: "Noelm307#",
    },
})


let mailOptions = {
    from: "nguemechieu@live.com",
    to: "",
    subject: "Sending Email using CryptoInvestor",
    text: "resetPassword",
}

transporter.sendMail(mailOptions, function (error, info) {
    if (error) {
        console.log(error)
    } else {
        console.log("Sent: " + info.response)
    }
})

router.post("/forgotPassword", (req, res) => {
    const randomPassword = (Math.random() *10).toString()+"noel";

    res.send()
    res.send(" new temp password has been send to your email address"+randomPassword);
})
;
module.exports =router;