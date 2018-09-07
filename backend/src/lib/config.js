const nconf = require('nconf');
nconf.formats.yaml = require('nconf-yaml');

nconf
    .env('__') // Environment overrides config.yaml
    .file({file: './config.yaml', format: nconf.formats.yaml});

module.exports = nconf;
