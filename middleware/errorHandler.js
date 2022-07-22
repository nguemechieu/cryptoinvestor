const { logEvents } = require('./logEvents');

let errorHandler =async (req, res, next,err) => {
    logEvents(`${err.name}: ${err.message}`, 'errLog.txt').then();
    console.error(err.stack)
    res.status(500).send(err.message);
}

module.exports = errorHandler;