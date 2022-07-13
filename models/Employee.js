


const { DataTypes } = require('sequelize');
//
module.exports = Employee;

function    Employee(sequelize) {
    const attributes = {
        "dateHire": { type: DataTypes.STRING, allowNull: false},
        email: { type: DataTypes.STRING, allowNull: false },
        passwordHash: { type: DataTypes.STRING, allowNull: false },
        confirmPassword:{ type: DataTypes.STRING, allowNull: false},
        firstName: { type: DataTypes.STRING, allowNull: false },
        middleName: { type: DataTypes.STRING, allowNull: false },
        lastName: { type: DataTypes.STRING, allowNull: false },
        role: { type: DataTypes.STRING, allowNull: false }
    };

    const options = {
        defaultScope: {
            // exclude password hash by default
            //attributes: { exclude: ['passwordHash'] }
        },
        scopes: {
            // include hash with this scope
            withHash: { attributes: {}, }
        }
    };

    return sequelize.define('Employee', attributes, options);
}






