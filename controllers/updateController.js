const bcrypt = require("bcrypt");
const {db} = require("../_helpers/db");

exports.update = async (req, res, next) => {

    db.User.findOne({ where: { email: req.body.email } })
        .then(person => {
            if(!person){
                db.User.update(

                    {...req.body},
                    {returning: true, where: {id: req.params.id} }
                )
                    .then(() => {
                        if (req.body.password) {
                            req.body.passwordHash = bcrypt.hash(req.body.password, 10);
                        }
                        return res.status(200).json({message: 'User updated successfully!'})
                    })
            }
            else {
                return res.json({message: 'Email has been already used!'})
            }
        })
        .catch(next)

}
