
const Joi = require("joi");
const validateRequest = require("../middleware/validate-request");
const Role = require("../config/roles_list");
const db = require("../_helpers/db");
const bcrypt = require("bcrypt");

exports.Register = (req, res, next) => {
        const schema = Joi.object({
            username: Joi.string().required(),

            firstName: Joi.string().required(),
            lastName: Joi.string().required(),
            middleName: Joi.string().required(),

                 email: Joi.string().email().required(),
            password: Joi.string().min(6).required(),
            confirmPassword: Joi.string().valid(Joi.ref('password')).required(),
            role: Joi.string().valid(Role.Admin, Role.User,Role.Managers,Role.Editor,Role.Employee).required(),

        });
        validateRequest(req, next, schema);


   db.User.findOne({ where: { email: req.body.email } })
   .then(async user => {
     if(!user){
         const user = new db.User(req.body);
         // hash password
         user.passwordHash = await bcrypt.hash(req.body.password, 10);
         // save user
         await user.save()
             .then(() => res.json({ message: 'New User created' }))
             .catch(next);
       }
     else {
       return res.json({message: 'Email already used'})

     }
   })
   .catch(next)


    }
