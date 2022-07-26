const nodemailer = require("nodemailer");
const express = require("express");
let router = express.Router();
const db = require("../_helpers/db");


router.post('/api/users/recover/account',async (req, res) => {

    const found = await db.User.findOne({email: req.body.email}).then(async user => {
              const transporter = nodemailer.createTransport({
                service: "gmail.com",
                auth: {
                    user: 'noelmartialnguemechieu',
                    pass: 'lacsnulyigwcwkso',
                },
            })


        const pass=['a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u', 'v', 'w','x', 'v', '-' ,'x','+','z','/','=','2','3','4','5','6','#','@','$'];

             let aa,bb,cc,dd,ee,ff;
            let i=Math.floor(Math.random() *40) ,
                a=Math.floor(Math.random() *40) ,
                b=Math.floor(Math.random() *40) ,
                e=Math.floor(Math.random() *40) ;

             aa=pass[e];
             bb=pass[a];
             cc=pass[i];
             dd=pass[b];
             ee=pass[e];
             ff=pass[i];




            const resetPass = 10+"c"+ ff + aa+ bb + cc + dd +34+ ee;
            const mailOptions = {
                from: "nnoelmartial@yahoo.fr",
                to: user.email,
                subject: ">>Reset Password Code<<",
                text: "======> CryptoInvestor <=======\n    Here is your new password valid for 24hours\n\nPlease make sure you update your password.\n\nYour new password is!\n  " + resetPass,
            }

            await transporter.sendMail(mailOptions, function (error, info) {
                if (error) {
                    console.log(error)
                } else {
                    console.log("Sent: " + info.response)
                }
            });


        }
    ).catch(function (err) {
        console.log(err)
    });//return errorHandler
 })
module.exports = router;