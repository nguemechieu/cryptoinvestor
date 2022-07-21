module.exports = {

    HOST: "db-mysql-nyc1-57018-do-user-11605945-0.b.db.ondigitalocean.com",

    USER: "doadmin",

    PASSWORD: "AVNS_49YtICxzVJiOyngmFfr",

    DB: "cryptoInvestors",

    PORT:25060,
    dialect: "mysql",

    pool: {

        max: 5,

        min: 0,

        acquire: 30000,

        idle: 10000

    }

};