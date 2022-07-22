
const Joi = require("joi");
const validateRequest = require("../middleware/validate-request");
const Role = require("../config/roles_list");

exports.signup = (req, res, next) => {
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