
let { DataTypes} = require('sequelize');
//
module.exports = modelEmployee;

function modelEmployee(sequelize) {
        const attributes = {id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true, allowNull: false},
                username:{ type: DataTypes.STRING, allowNull: false },
                firstname: { type: DataTypes.STRING, allowNull: false },
                lastname: { type: DataTypes.STRING, allowNull: false },
                middleName:{ type: DataTypes.STRING, allowNull: false },
                email:{ type: DataTypes.STRING, allowNull: false },
                telephone:{ type: DataTypes.STRING, allowNull: false },
                address:{ type: DataTypes.STRING, allowNull: false },
                city: { type: DataTypes.STRING, allowNull: false },
                state: { type: DataTypes.STRING, allowNull: false },
                zip: { type: DataTypes.STRING, allowNull: false },
                passwordHash:{ type: DataTypes.STRING, allowNull: false },
                confirmedPassword: { type: DataTypes.STRING, allowNull: false },
                role: { type: DataTypes.STRING, allowNull: false },
                dateHires:{ type: DataTypes.STRING, allowNull: false },
                dateUpdated: { type: DataTypes.STRING, allowNull: false },

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

