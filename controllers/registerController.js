const bcrypt = require("bcrypt");



const db = require("../_helpers/db");
const Joi = require("joi");
const validateRequest = require("../middleware/validate-request");
const Role = require("../_helpers/role");

exports.signup =
    exports.createUser= (req, res, next) => {
        const schema = Joi.object({
            username: Joi.string().required(),
            firstName: Joi.string().required(),
            middleName: Joi.string().required(),
            lastName: Joi.string().required(),
                 email: Joi.string().email().required(),
            password: Joi.string().min(6).required(),
            confirmPassword: Joi.string().valid(Joi.ref('password')).required(),
            role: Joi.string().valid(Role.Admin, Role.User,Role.Managers,Role.Editor,Role.Employee).required(),

        });
        validateRequest(req, next, schema);
    }