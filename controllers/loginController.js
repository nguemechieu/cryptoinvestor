const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const {db} = require("../_helpers/db");


exports.login = (req, res, next) => {
    db.User.findOne({ where: { email: req.body.email } })
        .then(user => {
            console.log(user)
            if (!user) {
                return res.status(401).json({ error: 'User not found!' });
            }
            bcrypt.compare(req.body.password, user.passwordHash)
                .then(valid => {

                    if (!valid) {
                        return res.status(401).json({ error: 'Mot de passe incorrect !' });
                    }
                    res.status(200).json({
                        userId: user.id,
                        token: jwt.sign(
                            {userId: user._id},
                            'RANDOM_TOKEN_SECRET',
                            {expiresIn: '24h'},res.redirect('/auth/home')
                        )
                    });
                })
                .catch(error => res.status(500).json({ error }));
        })
        .catch(error => res.status(500).json({ error }));
};
