
const  express = require("express");


const router = express.Router();

router.route("/forgotPassword").post( (req, res) => {
    res.render('forgotPassword', { title    :"Recover your account"
    })
})
;
module.exports =router;