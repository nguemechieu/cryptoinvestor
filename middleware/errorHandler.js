const { logEvents } = require('./logEvents');

const errorHandler = (err, req, res) => {
    logEvents(`${err.name}: ${err.message}`, 'errLog.txt').then(r => "");
    console.error(err.stack)
    res.status(500).send(err.message);
}

module.exports = errorHandler;