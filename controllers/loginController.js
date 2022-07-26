
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const db = require("../_helpers/db");
const validateRequest = require("../middleware/validate-request");

exports.loginController=  async  (req, res,next)=>  {
    const cookies = req.cookies;
    console.log(`cookie available at login: ${JSON.stringify(cookies)}`);


    let Joi= require('joi');
    const schema = Joi.object({
        username    : req.body.username,

        role: Joi.string().required(),
       password: Joi.string().required(),
    });
    validateRequest(req, next, schema);

const pwd = req.body.password;

    const foundUser = await db.User.findOne({ where:{  username: req.body.username, role: req.body.role}});
    if (!foundUser) return res.status(403).send({ messages: {"error" : "user not found"}   }); //Unauthorized
    // evaluate password
    const match = await bcrypt.compare(pwd, foundUser.password);
    if (match) {
        const roles = Object.values(foundUser.role).filter(Boolean);
        // create JWTs
        const accessToken = jwt.sign(
            {
                "User": {
                    "username": foundUser.username,
                    "role": roles
                }
            },
            process.env.ACCESS_TOKEN_SECRET,
            { expiresIn: '10s' },next
        );
        const newRefreshToken = jwt.sign(
            { "username": foundUser.username },
            process.env.REFRESH_TOKEN_SECRET,
            { expiresIn: '1d' },next
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
            const foundToken = await db.User.findOne({where: {refreshToken: refreshToken}});

            // Detected refresh token reuse!
            if (!foundToken) {
                console.log('attempted refresh token reuse at login!')
                // clear out ALL previous refresh tokens
                newRefreshTokenArray = [];
            }

            res.clearCookie('jwt', { httpOnly: true, sameSite: 'None', secure: true });
        }

        // Saving refreshToken with current user
        foundUser.refreshToken = [...newRefreshTokenArray, newRefreshToken];
        const result = await foundUser.save();
        console.log(result);
        console.log(roles);

        // Creates Secure Cookie with refresh token
        res.cookie('jwt', newRefreshToken, { httpOnly: true, secure: true, sameSite: 'None', maxAge: 24 * 60 * 60 * 1000 });

        // Send authorization roles and access token to user
        res.json({ roles, accessToken });

    } else {
        res.status(403).json({ status: 'Invalid username or password!' });
    }
}
