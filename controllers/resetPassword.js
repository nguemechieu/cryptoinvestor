const {db} = require("../_helpers/db");

exports.resetPassword = function (req, res){
    req.body.password;
    res.send("Reset password now");
    let oldPassword = document.getElementById("oldPassword").value, newPassword = document.getElementById("newPassword").value,
        confirmPassword = document.getElementById("confirmPassword").value;
    if(oldPassword!=null&&newPassword!=null&&confirmPassword!=null)
    {
        if(oldPassword!==newPassword)
        {
            if(newPassword===confirmPassword)
            {
                db.User.findOne({where:{ password:  req.body.password}})
                    .then(function (user) {
                        user.password = newPassword;

                    })
                return true;
            }
            else
            {
                alert("Confirm password is not same as you new password.");
                return false;
            }
        }
        else
        {
            alert(" This Is Your Old Password,Please Provide A New Password");
            return false;
        }
    }
    else
    {
        alert("All Fields Are Required");
        return false;
    }

}