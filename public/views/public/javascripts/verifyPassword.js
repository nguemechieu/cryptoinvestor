
function check(req, res) {


    let password = document.getElementById('password').value;
    let confirmPassword = document.getElementById('confirmPassword').value;
    if (confirmPassword !== password) {
        res.send(" Password Not matches! Please try again")


    }
}
module.exports.check = check;