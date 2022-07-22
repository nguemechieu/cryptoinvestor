const express = require('express');
const router = express.Router();
const usersController = require('../../controllers/usersController');
const ROLES_LIST = require('../../config/roles_list');
const verifyRoles = require('../../middleware/verifyRoles');

router.route('/users')
    .get(usersController.getAll)
    .get(verifyRoles(ROLES_LIST.Admin), usersController.updateSchema)
    .delete(verifyRoles(ROLES_LIST.Admin), usersController.delete);

router.route('/users/:id')
    .get(verifyRoles(ROLES_LIST.Admin), usersController.getById);

module.exports = router;