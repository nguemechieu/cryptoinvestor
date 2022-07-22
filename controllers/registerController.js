const bcrypt = require("bcrypt");



const db = require("../_helpers/db");

exports.signup =  async (req, res) => {
    const { user, pwd } = req.body;
    if (!user || !pwd) return res.status(400).json({ 'message': 'Username and password are required.' });

    // check for duplicate usernames in the db
    const duplicate = await db.User.findOne({ username: user }).exec();
    if (duplicate) return res.sendStatus(409); //Conflict

    try {
        //encrypt the password
        const hashedPwd = await bcrypt.hash(pwd, 10);

        //create and store the new user
        const result = await db. User.create({
            "username": user,
            "password": hashedPwd,
            "email": user.email,
            "firstName": user.firstName,
            "lastName": user.lastName,
            "middleName": user.middleName,
            "role": user.role
        });

        console.log(result);

        res.status(201).json({ 'success': `New user ${user} created!` });
    } catch (err) {
        res.status(500).json({ 'message': err.message });
    }
}
