const Joi = require("joi");
const validateRequest = require("../middleware/validate-request");
const {DataTypes} = require("sequelize");

const ProfileModel=
    function ProfileModel(sequelize) {
        const attributes = {
            id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true, allowNull: false},
            email: { type: DataTypes.STRING, allowNull: false },
            firstName: { type: DataTypes.STRING, allowNull: false},
            middleName: { type: DataTypes.STRING, allowNull: false},
            lastName: { type: DataTypes.STRING, allowNull: false},
            username: { type: DataTypes.STRING, allowNull: false },


            role: { type: DataTypes.STRING, allowNull: false},

        };

        const options = {
            defaultScope: {
                // exclude password hash by default
                //  attributes: { exclude: ['passwordHash'] }
            },
            scopes: {
                // include hash with this scope
                withHash: { attributes: {}, }
            }
        };

        return sequelize.define('ProfileModel', attributes, options);
    }


module.exports = ProfileModel;
