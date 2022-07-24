const joi = require("joi");
const bcrypt = require("bcrypt");
const {UserModel} = require("../model/User");
const comFunc = require("../_helpers/commonFuns");
const db = require("../_helpers/db");
const {jwt} = require("twilio");

exports.signup = async (req, res) => {
    let password1;
    let email;
    try {
        let {data} = req.body;
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
        const result = schema.validate(req.body, {abortEarly: true});
        if (!result) {
            return res.status(404).json(result.error);

        }
       // res.status(200).json({message: "validation passed", data: req.body});

        password1 = req.body.password;
        email = req.body.email;
        let otp = 141234;

        let mobileExist = await db.User.findOne({where:{ phone: req.body.phone, email: email}});
        if (mobileExist) {
            return new Error("This phone number is already in register!. Please try a new phone number");
        }
        let checkMail = await db.User.findOne({where: {email}});
        if (checkMail) {
            return new Error("This email address is already registered!!");
        }

        // let access_token=comFunc.generateAcessToken(10);
        let message = "your otp is:" + otp
        console.log(message)

        await comFunc.sendotp(message, req.body.country_code + req.body.phone)

        const cookies = req.cookies;
        if (!cookies?.jwt) return res.sendStatus(204); //No content
        const refreshToken = cookies.jwt;


        let save = {
            username    : req.body.username,
            firstName: req.body.firstName,
            lastName: req.body.lastName,
            middleName: req.body.middleName,
            email: req.body.email,
            phone: req.body.phone,
            country_code: req.body.country_code,
            age: req.body.age,
            role: req.body.role,
            password: password1,
            confirmPassword: req.body.confirmPassword,
            access_token: "noel405671@",
            refreshToken: refreshToken
        };
        console.log(save);
        let user = await db.User.create(save);
        if (user) {res.status(200).json({message: "User registered successfully"});




       res.redirect('/');

        } else {res.status(404).json({message: "user not registered"});


        }
        // var user_update = await user.save();
        // if (user_update) {
        //   res.status(200).json({
        //     message: "account is created",
        //     response: user_update,
        //   });
        // }

    } catch (error) {
        res.status(400).json({message: error.message});
    }
};

