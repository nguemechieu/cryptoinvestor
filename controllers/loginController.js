const joi = require("joi");

const User = require('../model/User');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const db = require("../_helpers/db");

module.exports =  login = async (req, res , next) => {
    const cookies = req.cookies;

    const schema = joi.object().keys({
        email: joi.string().required(),
        password: joi.string().required(),
        role: joi.string().required()
    });
    const result = schema.validate(req.body, {abortEarly: true});
    if (result.errors) {
        return result.errors
    }





        const foundUser = await db.User.findOne({where:{email: req.body.email}});
        if (!foundUser) return res.sendStatus(401); //Unauthorized
        // evaluate password
        const match = await bcrypt.compare(req.body.password, foundUser.password);
        if (match) {
            const roles = Object.values(foundUser.roles).filter(Boolean);
            // create JWTs
            const accessToken = jwt.sign(
                {
                    "User": {
                        "username": foundUser.username,
                        "role": roles,

                    },
                },
                process.env.ACCESS_TOKEN_SECRET,
                {expiresIn: '10s'},""
            );
            const newRefreshToken = jwt.sign(
                {"username": foundUser.username},
                process.env.REFRESH_TOKEN_SECRET,
                {expiresIn: '15s'},""
            );

            // Changed to let keyword
            let newRefreshTokenArray =
                !cookies?.jwt
                    ? foundUser.refreshToken
                    : foundUser.refreshToken.filter(rt => rt !== cookies.jwt);

            if (cookies?.jwt) {

                /*
                Scenario added here:
                    1) User logs in but never uses RT and does not logout
                    2) RT is stolen
                    3) If 1 & 2, reuse detection is needed to clear all RTs when user logs in
                */
                const refreshToken = cookies.jwt;
                const foundToken = await db.User.findOne({where:{refreshToken}})

                // Detected refresh token reuse!
                if (!foundToken) {
                    // clear out ALL previous refresh tokens
                    newRefreshTokenArray = [];
                }

                res.clearCookie('jwt', {httpOnly: true, sameSite: 'None', secure: true});
            }

            // Saving refreshToken with current user
            foundUser.refreshToken = [...newRefreshTokenArray, newRefreshToken];
            const result = await foundUser.save();

            // Creates Secure Cookie with refresh token
            res.cookie('jwt', newRefreshToken, {
                httpOnly: true,
                secure: true,
                sameSite: 'None',
                maxAge: 24 * 60 * 60 * 1000
            });

            // Send authorization roles and access token to user
            res.json({accessToken});

        } else {
            res.sendStatus(401);
        }

}
