const express = require('express');
const router = express.Router();
const employeesController = require('../../controllers/employeesController');
const ROLES_LIST = require('../../config/roles_list');
const verifyRoles = require('../../middleware/verifyRoles');

router.route('/api/admin/employee')
    .get(employeesController.getAllEmployees)
    .post(verifyRoles(ROLES_LIST.admin, ROLES_LIST.editor), employeesController.createNewEmployee)
    .put(verifyRoles(ROLES_LIST.admin, ROLES_LIST.editor), employeesController.updateEmployee)
    .delete(verifyRoles(ROLES_LIST.admin), employeesController.deleteEmployee);

router.route('/api/employees/:id')
    .get(employeesController.getEmployee);

module.exports = router;