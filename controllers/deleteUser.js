const db = require("../_helpers/db");

exports.delete = (req, res, next) => {
    db.User.findByPk(req.params.id)
        .then(person =>{
            if (!person) {
                return res.status(401).json({ error: 'User not found!' });
            }
            person.destroy()
                .then(() => res.status(200).json({ message: 'User deleted successfully' }))
                .catch(next);
        })
    ;res.redirect('/');
}


exports.deleteUser = (req, res, next) => {
    if (req.body===db.User) return next
    {
        res.sendDate();
        res.json("User 'deleted successfully ");
        next(
        res.redirect('/'))
    }




}