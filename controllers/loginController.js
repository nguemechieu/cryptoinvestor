const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const {db} = require("../_helpers/db");

const Joi = require('joi')

//User-defined function to validate the user
function validateUser(user)
{
    const JoiSchema = Joi.object({

        username: Joi.string()
            .min(5)
            .max(30)
            .required(),

        email: Joi.string()
            .email()
            .min(5)
            .max(50)
            .optional(),

        date_of_birth: Joi.date()
            .optional(),

        account_status: Joi.string()
            .valid('activated')
            .valid('unactivated')
            .optional(),
    }).options({ abortEarly: false });

    return JoiSchema.validate(user)
}

const user = {
    username: 'nguemechieu',
    email: 'nguemechieu@live.com',
    date_of_birth: '1990-4-3',
    role: 'Admin',
    account_status: 'activated'
}

response = validateUser(user)

if(response.error)
{
    console.log(response.error.details)
}
else
{
    console.log("Validated Data")
}
exports.login = (req, res, next) => {
    db.User.findOne({ where: { email: req.body.email } })
        .then(user => {
            console.log(user)
            if (!user) {
                return res.status(401).json({ error: 'User not found!' });
            }
            bcrypt.compare(req.body.passwordHash, db.User.passwordHash)
                .then(valid => {

                    if (!valid) {
                        return res.status(401).json({ error: 'Mot de passe incorrect !' });
                    }
                    res.status(200).json({
                        userId: user.id,
                        token: jwt.sign(
                            {userId: user.id},
                            'RANDOM_TOKEN_SECRET',
                            {expiresIn: '24h'},res.redirect('/auth/home')
                        )
                    });
                })
                .catch(error => res.status(500).json({ error }));
        })
        .catch(error => res.status(500).json({ error }));
};
