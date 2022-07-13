

const User = require('../models/User');

const config = require('../config.database.json');
const mysql = require('mysql2/promise');
const { Sequelize } = require('sequelize');



module.exports = db = {};

initialize().then(r => "");

class Employee extends User{
    constructor(sequelize) {
        super(User, User, User, sequelize);

    }

}

async function initialize(){

    // Initialisation de toutes les sauvegardes concernant les établissement

    // create db if it doesn't already exist
    const { host, port, user, password } = config.database;
    const database = config.database.database;
    const connection = await mysql.createConnection({ host, port, user, password });
    await connection.query(`CREATE DATABASE IF NOT EXISTS \`${database}\`;`);

    // connect to db
    const sequelize = new Sequelize(database, user, password, {dialect: 'mysql'});



    db.User = new User(sequelize);
    db.Employee = new Employee(sequelize);



    await sequelize.sync({ alter: true });
}

console.log(db.UserInfo);