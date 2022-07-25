
const  express = require('express');
const router = express.Router() ;


router.get("/api/users/signup",(req, res) => {
res.render('register', { title : 'Registration'})
});
module.exports = router;