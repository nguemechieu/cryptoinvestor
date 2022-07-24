let { UserModel } = require("../model/User");
const jwt=require('jsonwebtoken');
exports.requireToken=async (req,res,next) => {
try{

        // console.log('here');
        let{access_token}=jwt+req.headers;
        console.log(access_token);
        //jwt.verify(req.access_token,'TOKEN_SECRET',(err,authorisedData))
        if(access_token){
            let userdata=await UserModel.findOne({access_token:access_token}).exec();
            if(userdata){
                req.userdata=userdata;
                console.log(userdata);
                 console.log("token verified");
                next()
            }else{
                return res.status(401).json({message:"Login Expired"});
            }

        }else{
            return res.status(401).json({'message':'access_token is required'})
        }

}
catch(err){
    res.status(400).json({msg:err})
    console.log(err);
}}