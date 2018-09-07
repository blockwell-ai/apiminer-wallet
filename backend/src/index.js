global.Promise = require('bluebird');
const log = require('./lib/logging');

if (!process.env.NODE_ENV) {
    process.env.NODE_ENV = 'development';
}

const app = require('./app');
const http = require('http');

let port = process.env.PORT || '3000';
app.set('port', port);

const server = http.createServer(app);
server.listen(port);

log.info(`Listening on port ${port}`);
