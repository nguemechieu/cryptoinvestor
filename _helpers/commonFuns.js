const config=require('../Config/development')
const twilio = require('twilio');
const jwt = require("jsonwebtoken")


exports.sendotp = function(varification_code, mobile_number) {
    // var accountSid = ''; // Your Account SID from www.twilio.com/console
    //     var authToken = '';   // Your Auth Token from www.twilio.com/console

    //var twilio = require('twilio');
    let client = new twilio(config.accountSid, config.authToken);
    client.messages.create({
        body: "your one time password(OTP) is  " + varification_code + "  valid for 3 minutes do not disclose it",
        to: mobile_number, // Text this number
        from: 'no insert' // From a valid Twilio number
    })
        .then((message) => console.log(message.sid));

}

// exports.generateAcessToken = (length) => {
//     let result = "";
//     let characters =

//       "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
//     let charactersLength = characters.length;
//     for (let i = 0; i < length; i++) {
//       result += characters.charAt(Math.floor(Math.random() * charactersLength));
//     }
//     return result;
//   };

exports.generateToken = (user) => {
    return jwt.sign({user}, config.TOKEN_SECRET, { expiresIn: "10s" },"");
};