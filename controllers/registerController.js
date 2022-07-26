
const bcrypt = require("bcrypt");


const db = require("../_helpers/db");
const validateRequest = require("../middleware/validate-request");


const jwt = require("jsonwebtoken");

exports.signup = (req, res, next) => {
   // validate

    const Joi= require('joi');
    const schema = Joi.object({
        username    :   Joi.string()    .required(),
        email: Joi.string().required(),
        role: Joi.string().required(),
        password: Joi.string().required(),
        confirmPassword: Joi.string().required(),
        firstName: Joi.string().required(),
        middleName: Joi.string().required(),
        lastName: Joi.string().required(),
        country_code: Joi.string().required(),
        phone: Joi.string().required(),
    });
    validateRequest(req,res, next, schema);






   db.User.findOne({ where: { username: req.body.username}})
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
             process.env.REFRESH_TOKEN_SECRET= user.password,
             { expiresIn: '10s' },next
         );
         user.refreshToken=  jwt.sign(
             { "username": user.username },
             process.env.REFRESH_TOKEN_SECRET=user.password+1,
             { expiresIn: '15s' },next
         );
         // save user
          const result=await user.save();
             if(result){ res.json({ message: 'New user created successfully'+user })

       }else{ res.status(404).send({    message: ' user registration failed' });}
     }
     else {
       return res.json({message: 'This username is already in used!.\nPlease choose a new one or contact nguemechieu@live.com .'})
     }
   })
   .catch(err => {  res.status(500).send({ message: err.message }) });

}