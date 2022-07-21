const authController = require("../controllers/usersController");
const router = require("./root");

router.route('/auth/home').get(authController.homePage);

