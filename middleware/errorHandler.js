const { logEvents } = require('./logEvents');

const errorHandler =ErrorHandler;

    function ErrorHandler(req, res,next, err){
    logEvents(`${err.name}: ${err.message}`, 'errLog.txt').then();
    console.error(err.stack)
    res.status(500).send(err.message);
    res.challenge(err.challenge);
}

module.exports = errorHandler;