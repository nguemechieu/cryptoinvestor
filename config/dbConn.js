const config = require('../config.database.json');
const mysql = require('mysql2/promise');
const { Sequelize } = require('sequelize');

const User = require('../models/User');

const Employee = require('../models/Employee');



module.exports = db = {};
module.exports=db.User;
module.exports=db.Employee;
initialize().then(r => "");



async function initialize(){

    // Initialisation de toutes les sauvegardes concernant les établissement

    // create db if it doesn't already exist
    const { host, port, user, password } = config.database;
    const database = config.database.database;
    const connection = await mysql.createConnection({ host, port, user, password });
    await connection.query(`CREATE DATABASE IF NOT EXISTS \`${database}\`;`);

    // connect to db
    const sequelize = new Sequelize(database, user, password, {dialect: 'mysql'});

    db.User =  User(sequelize);
    db.Employee =  Employee(sequelize);

    await sequelize.sync({ alter: true });
}

console.log(db.User);