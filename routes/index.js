const express = require('express');
const router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

/* GET register page. */
router.post('/register', function(req, res, next) {
  res.render('register', { title: 'Registration' });
});


const registerController = require('../controllers/registerController');


router.post('/auth/register', registerController.signup);










module.exports = router;
