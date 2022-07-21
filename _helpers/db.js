const dbConfig = require("../_helpers/db.config.js");

const { Sequelize } = require('sequelize');

const mysql = require("mysql2/promise");
const User = require("../model/User");


const Employee = require('../model/Employee');
const db={};
initialize().then( );
module.exports = db;
async function initialize(){const    host= dbConfig.HOST, port=dbConfig.PORT, user=dbConfig.USER, password=dbConfig.PASSWORD
    const database = dbConfig.DB;
const connection = await mysql.createConnection({ host, port, user, password });
await connection.query(`CREATE DATABASE IF NOT EXISTS \`${database}\`;`);
const sequelize = new Sequelize(database, user, password,{
    host: host,port: port,
    dialect: dbConfig.dialect,
    operatorsAliases: false,

    pool: {
        max: dbConfig.pool.max,
        min: dbConfig.pool.min,
        acquire: dbConfig.pool.acquire,
        idle: dbConfig.pool.idle
    }
});

    db.sequelize = sequelize
    db.Sequelize = Sequelize
    db.User=User(sequelize);

    db.Employee=Employee(sequelize);

    await sequelize.sync({ alter: true });
}

