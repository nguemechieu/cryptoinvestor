
const {db} = require("../_helpers/db");
const bcrypt = require("bcrypt");
const shema= require('../controllers/usersController')

exports.signup = async (req, res, next) => {


    // validate
    db.User.findOne({ where: { email: req.body.email } })
        .then(async user => {
            if(!user){
                shema.createSchema({ schema : { email: req.body.email } })
             user= db.User[shema];



                // hash password
                //

               // const confirmPassword= await bcrypt.hash(user.confirmPassword, 10);

                // save user
                //
                //  await db.User.save(user)
                //     .then(() => res.json({ message: 'User'+user.username+' created' }))
                //      .catch(next);

                res.json({ message: 'User created', user: user });


            }
            else {res.json({message: 'Email already used'})
              return   res.redirect('/');
            }
        })
        .catch(next)

}
