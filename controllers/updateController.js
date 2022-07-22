const bcrypt = require("bcrypt");
const {db} = require("../_helpers/db");

exports.update = async (req, res, next) => {

    db.User.findOne({ where: { email: req.body.email } })
        .then(user => {
            if(!user){
                db.User.update(
                    {...req.body},
                    {returning: true, where: {id: req.params.id} }
                )
                    .then(() => {
                        if (req.body.password) {
                            req.body.passwordHash = bcrypt.hash(req.body.password, 10);
                        }
                        return res.json({message: 'User updated'})
                    })
            }
            else {
                return res.json({message: 'Email already used'})
            }
        })
        .catch(next)

}