const bcrypt = require("bcrypt");
const {db} = require("../_helpers/db");

exports.signup = async (req, res, next) => {

    // validate
    db.User.findOne({ where: { email: req.body.email } })
        .then(async user => {
            if(!user){
                const data ={

                    email: req.body.email,
                    passwordHash: req.body.passwordHash,
                    confirmPassword: req.body.confirmPassword,
                    username: req.body.username,
                    firstName: req.body.firstName,
                    lastName: req.body.lastName,
                    middleName: req.body.middleName,
                    role: req.body.role




                }
                const user = db.User[data];
                // hash password
                //

                let password= await bcrypt.hash(user.passwordHash, 10);
                // save user
                await user.save(password)
                    .then(() => res.json({ message: 'User'+user.username+' created' }))
                    .catch(next);
            }
            else {
                return res.json({message: 'Email already used'})
            }
        })
        .catch(next)

}
