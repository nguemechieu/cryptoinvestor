
const dbConfig = require("../_helpers/db.config.js");

const mysql = require("mysql2/promise");


const {Sequelize} = require("sequelize");
const db={};
const User = require("../model/User");
const Employee = require("../model/Employee");
//Importing the process module
const process = require('process');

// Printing object for process.env
let no_env = 0;

// Calling the process.env function
let env = process.env;

// Traversing through the returned data
for (let key in env) {
    // Printing values
    console.log(key + ":\t\t\t" + env[key]);
    no_env++;
}

// Printing total count
console.log("total no of values available = " + no_env);

// Accessing fields one by one
console.log("operating system: " + env['OS']);
console.log("alluserprofile: " + env['ALLUSERSPROFILE']);
console.log("public directory: " + env['PUBLIC']);

DataBaseRun().then();

//
async function DataBaseRun(){
    let   host= dbConfig.HOST, port=dbConfig.PORT, user=dbConfig.USER, password=dbConfig.PASSWORD,


     database = dbConfig.DATABASE;
    const connection = await mysql.createConnection(

        { host, port, user, password }



    );
    await connection.query(`CREATE DATABASE IF NOT EXISTS \`${database}\`;`);


    const sequelize = new Sequelize(database, user, password,{
        host: dbConfig.HOST,port: dbConfig.PORT,
        dialect: dbConfig.dialect,


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

module.exports = db;
exports. Employee = db.Employee;
exports.User = db.User;