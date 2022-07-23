const { logEvents } = require('./logEvents');

const errorHandler = async (err, req, res, next) => {
    logEvents(`${err.name}: ${err.message}`, 'errLog.txt').then(r => "");
    console.error(err.stack)
    res.status(500).send(err.message);
}

module.exports = errorHandler;