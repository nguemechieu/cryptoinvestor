const ProfileModel = require("../model/ProfileModel");


exports.CreateProfile = (req, res) => {

    let reqBody = req.body;
    ProfileModel.create(reqBody, (err, data) => {
        if(err){
            res.status(400).json({status: "Failed to user create", data: err})
        }else{
            res.status(200).json({status: "Successfully user created", data: data})
        }
    })
}