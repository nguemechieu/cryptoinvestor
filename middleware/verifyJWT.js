
const verifyJWT = (req, res, next) => {
    const jwt = require('jsonwebtoken');

    const authHeader = req.headers.authorization||req.headers.Authorization ;
    if (!authHeader?.startsWith('Bearer ')) return res.sendStatus(401);
    const token = authHeader.split(' ')[1];
    console.log(token);
    jwt.verify(
        token,
        process.env.ACCESS_TOKEN_SECRET,
        (err, decoded) => {

            if (err) return res.sendStatus(403); //invalid token
            req.email = decoded.User.email;
            req.password = decoded.User.password;


            req.role = decoded.User.role
        },next

    );
}

module.exports = verifyJWT