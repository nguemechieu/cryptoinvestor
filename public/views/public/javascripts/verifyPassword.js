
function check(req, res, next) {


    let password = document.getElementById('password').value;
    let confirmPassword = document.getElementById('confirmPassword').value;
    if (confirmPassword !== password) {
        res.send(" Password Not matches! Please try again")


    };next();
}
module.exports.check = check;