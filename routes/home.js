const authController = require("../controllers/loginController");
const router = require("./root");

router.route('/auth/home').get(authController.login);

