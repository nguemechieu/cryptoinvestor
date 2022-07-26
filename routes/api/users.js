const express = require('express');
const router = express.Router();
const usersController = require('../../controllers/usersController');
const ROLES_LIST = require('../../config/roles_list');
const verifyRoles = require('../../middleware/verifyRoles');

router.route('/api/users')
    .get(usersController.getAllUsers)
    .get(verifyRoles(ROLES_LIST.admin), usersController.getUser)
    .delete(verifyRoles(ROLES_LIST.admin), usersController.deleteUser);

router.route('/api/users/:id')
    .get(verifyRoles(ROLES_LIST.admin), usersController.getUserById);

module.exports = router;