const { UserModel } = require("../model/User");

const joi = require("joi");
const comFunc=require('../_helpers/commonFuns');
const {DataTypes} = require("sequelize");
const {config} = require("dotenv");
const bcrypt = require("bcrypt");
const db = require("../_helpers/db");
const {jwt} = require("twilio");

let checkUser;

exports.login = async (req, res) => {

        const schema = joi.object().keys({
            email: joi.string().required(),
            password: joi.string().required(),
            role: joi.string().required()
        });
        const result = schema.validate(req.body, { abortEarly: true });
if (result.errors) {return result.errors}
        let { email, password, access_token } = req.body;
      let  password1 = bcrypt.hash(password,10);

         checkUser = await db.User.findOne({email,password1 });
        //console.log(checkUser,"here")
        if (checkUser) {
            console.log(checkUser);
            const cookies = req.cookies;
            if (!cookies?.jwt) return res.sendStatus(204); //No content
            const refreshToken = cookies.jwt;

            access_token = jwt;

            checkUser = await db.User.findOne({access_token:access_token}).then(user => {

                user.update( access_token)   ;


                user.update({ id: checkUser.id });

                 //   { new: true })

            } );


        //    res.status(200).json({ message: "User login successfully!"});

            res.render("home.ejs", {    title: "Welcome to CryptoInvestor Application"})
            console.log(checkUser);
        }
        else {
            // throw new Error("not matched ");
            res.status(201).json({message:"User not matched!"})

        }






exports.updateprofile = async (req, res) => {

    //res.status(200).json({message:'accesstoken passed'})

    try {
        console.log("hi");
        const schema = joi.object().keys({
            email: joi.string(),
            firstName: joi.string(),

            lastName: joi.string(),

            password: joi.string(),

            phone: joi.number()
        });
        //  // console.log(schema);

        let result = schema.validate(req.body);
        //console.log(result);
        if (result.error) {
            throw new Error(result.error);

        }

// let { first_name,
//       last_name,
//       email,
//       mobaile_number,
//       password}
//       = req.body;
        // console.log(req.body)
        let user_id =  checkUser.id;

        let updateddata = await db.User.update(
            {id: user_id.id},
            {$set: req.body},
            {new: true}
        );
        console.log(updateddata);
        res.status(200).json({
            status: 1,
            message: "profile updated successfully",
            userdata: updateddata,
        });
    } catch (error) {
        res.status(401).json({status: 0, message: error.message});
    }
}}