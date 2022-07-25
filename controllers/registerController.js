
const bcrypt = require("bcrypt");


const db = require("../_helpers/db");
const validateRequest = require("../middleware/validate-request");


const jwt = require("jsonwebtoken");

exports.signup = (req, res, next) => {
   // validate

    const Joi= require('joi');
    const schema = Joi.object({
        email: Joi.string().required(),
        role: Joi.string().required(),
        password: Joi.string().required(),
        confirmPassword: Joi.string().required(),
        firstName: Joi.string().required(),
        middleName: Joi.string().required(),
        lastName: Joi.string().required(),
        phone: Joi.string().required(),

    });
    validateRequest(req, next, schema);






   db.User.findOne({ where: { email: req.body.email } })
   .then(async user => {
     if(!user){
         const user = new db.User(req.body);
         // hash password

         if(req.body.password!==req.body.confirmPassword){

             return next(new Error(`Password Not Matched: ${req.body.password}`));
         }

         user.password= await bcrypt.hash(req.body.password, 10);
         user.confirmPassword= await bcrypt.hash(req.body.confirmPassword, 10);
         user.access_token=  jwt.sign(
             { "username": user.username },
             process.env.REFRESH_TOKEN_SECRET,
             { expiresIn: '10s' },next
         );
         user.refreshToken=  jwt.sign(
             { "username": user.username },
             process.env.REFRESH_TOKEN_SECRET,
             { expiresIn: '15s' },next
         );
         // save user
         await user.save()
             .then(() => res.json({ message: 'New user created successfully'+user }))
             .catch(next);
       }
     else {
       return res.json({message: 'This email already exists .Please choose a new one or contact the administrator to update this email.'})
     }
   })
   .catch(next)

}