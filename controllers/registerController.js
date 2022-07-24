const joi = require("joi");
const bcrypt = require("bcrypt");


const db = require("../_helpers/db");
const validateRequest = require("../middleware/validate-request");

exports.signup = async (req, res,next) => {

    try {
        let data = req.body;
        const schema = joi.object().keys({

            username: joi.string().required(),
            email: joi.string().required(),
            password: joi.string().min(6).max(100).required(),
            confirmPassword: joi.string().required(),
            phone: joi.string().required(),
            country_code: joi.string().required(),
            firstName: joi.string().required(),
            lastName: joi.string().required(),
            middleName: joi.string().required(),
            age: joi.date().required(),
            role: joi.string().required()


        });
      validateRequest(req, next ,schema);


        // res.status(200).json({message: "validation passed", data: req.body});


        const {user, pwd} = req.body;

        // check for duplicate usernames in the db
        const duplicate = await db.User.findOne({where:{email:req.body.email}});
        if (duplicate) return res.status(409).json({messages: 'This email is already used'}); //Conflict

        try {
            //encrypt the password
            const hashedPwd = await bcrypt.hash(req.body.password, 10);

            //create and store the new user
            const result = await db.User.create({
                "username": user,
                "password": hashedPwd
            });

            console.log(result);

            res.status(201).json({'success': `New user ${user} created!`});
        } catch (err) {
            res.status(500).json({'message': err.message});
        }
    }catch (err) {  console.log(err);
        let result = err.message; }
}