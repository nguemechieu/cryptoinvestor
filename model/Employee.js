
let { DataTypes} = require('sequelize');
//

function Employee(sequelize) {
        const attributes = {

                id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true, allowNull: false},
                username:{ type: DataTypes.STRING, allowNull: false },
                email:{ type: DataTypes.STRING, allowNull: false },
                telephone:{ type: DataTypes.STRING, allowNull: false },
                address:{ type: DataTypes.STRING, allowNull: false },
                city: { type: DataTypes.STRING, allowNull: false },
                state: { type: DataTypes.STRING, allowNull: false },
                zip: { type: DataTypes.STRING, allowNull: false },
                password:{ type: DataTypes.STRING, allowNull: false },
                confirmedPassword: { type: DataTypes.STRING, allowNull: false },
                firstName: { type: DataTypes.STRING, allowNull: false },
                lastName: { type: DataTypes.STRING, allowNull: false },
                middleName:{ type: DataTypes.STRING, allowNull: false },
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
module.exports=Employee;
