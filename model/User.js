const {DataTypes} = require("sequelize");
const {config} = require("dotenv");


//

function User(sequelize) {
    const attributes = {
        id: { type: DataTypes.INTEGER,     allowNull: false , autoIncrement: true, primaryKey: true},
        username:{ type: DataTypes.STRING, allowNull: false },
        email:{ type: DataTypes.STRING, allowNull: false },
        password:{ type: DataTypes.STRING, allowNull: false },
        confirmPassword:{ type: DataTypes.STRING, allowNull: false  },
        firstName: { type: DataTypes.STRING, allowNull: false },
        lastName: { type: DataTypes.STRING, allowNull: false },
        middleName:{ type: DataTypes.STRING, allowNull: false },
        age: { type: DataTypes.DATE, allowNull: false , defaultValue: '1980-04-03 02:04:05'    },
        role: { type: DataTypes.STRING, allowNull: false , defaultValue: 'user' },
         country_code: { type: DataTypes.STRING, allowNull: false},
        phone: { type: DataTypes.STRING, allowNull: false}
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

    return sequelize.define('User', attributes, options);
}
module.exports=User;
