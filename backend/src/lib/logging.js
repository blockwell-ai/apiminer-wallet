const winston = require('winston');

/**
 * @type Logger
 */
const log = winston.createLogger({
    level: 'debug',
    format: winston.format.json(),
    transports: [
        new winston.transports.Console()
    ]
});
module.exports = log;
