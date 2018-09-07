const config = require('../lib/config');
const rp = require('request-promise');
const users = require('../data/users');
const ApiMiner = require('./ApiMiner');

// API Miner access token
const apiToken = config.get('apiminer_token');

// Base URL of the API Miner environment
const baseUrl = config.get('apiminer_url');

// Request defaults
const request = rp.defaults({
    baseUrl: baseUrl,
    qsStringifyOptions: {
        arrayFormat: 'repeat'
    },
    json: true
});

/**
 * Get an ApiMiner client instance for the admin account.
 *
 * @returns {ApiMiner}
 */
function getAdminClient() {
    return new ApiMiner(apiToken, request);
}

/**
 * Get an ApiMiner client instance for the given user.
 *
 * @param {Object} user
 * @returns {Bluebird<ApiMiner>}
 */
function getClient(user) {
    return users.getApiminerToken(user.id)
        .then(token => {
            return new ApiMiner(token, request);
        });
}

module.exports = {
    getAdminClient,
    getClient
};
