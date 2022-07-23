
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const db= require("../_helpers/db");



exports.login = async (req, res, next) => {
    const cookies = req.cookies;

    const user= req.body
    const pwd = req.body.password;
    if (!pwd || !user) return res.status(400).json({ 'message': 'Username and password are required.' });

    const foundUser = await db.User.findOne({where:{ email: req.body.email }}).then()
    if (!foundUser) return res.status(403).json({ 'message': 'Incorrect email or password !' }); //Unauthorized
    // evaluate password
    const match = await bcrypt.compare(pwd, foundUser.password);
    if (match) {
        const roles = Object.values(req.body.role).filter(Boolean);
        // create JWTs
        const accessToken = jwt.sign(
            {
                "User": {
                    "username": req.body.username,
                    "email": req.body.email,
                    "password": pwd,
                    "role": roles
                }
            },
            "noel307",
            process.env.ACCESS_TOKEN_SECRET,
            { expiresIn: '10s' }
        );
        const newRefreshToken = jwt.sign(
            { "username": foundUser.username },
            process.env.REFRESH_TOKEN_SECRET,
            { expiresIn: '15s' },
        next())

        // Changed to let keyword
        let newRefreshTokenArray = !cookies?.jwt ? foundUser.refreshToken : foundUser.refreshToken.filter(rt => rt !== cookies.jwt);

        if (cookies?.jwt) {

            /*
            Scenario added here:
                1) User logs in but never uses RT and does not logout
                2) RT is stolen
                3) If 1 & 2, reuse detection is needed to clear all RTs when user logs in
            */
            const refreshToken = cookies?.jwt;
            const foundToken = await db.User.findOne({ refreshToken }).exec();

            // Detected refresh token reuse!
            if (!foundToken) {
                // clear out ALL previous refresh tokens
                newRefreshTokenArray = [];
            }

            res.clearCookie('jwt', { httpOnly: true, sameSite: 'None', secure: true });
        }

        // Saving refreshToken with current user
        foundUser.refreshToken = [...newRefreshTokenArray, newRefreshToken];
        let result = await foundUser.save();
        if(!result){
            res.status(404).json(result+ "user not saved!")

        }

        // Creates Secure Cookie with refresh token
        res.cookie('jwt', newRefreshToken, { httpOnly: true, secure: true, sameSite: 'None', maxAge: 24 * 60 * 60 * 1000 });

        // Send authorization roles and access token to user
        res.json({ accessToken });

    } else {
        res.sendStatus(401);
    }

}