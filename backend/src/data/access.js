/**
 * Database abstraction layer for access tokens.
 */

/**
 * @type {Knex}
 */
const db = require('../lib/db');
const UIDGenerator = require('uid-generator');
const uid = new UIDGenerator(512);

const tokenExpiration = 24 * 60 * 60 * 1000;

/**
 * Find an access token.
 *
 * @param {String} token Token to find.
 * @returns {Bluebird<Object>} Token data
 */
function find(token) {
    return db
        .first('user', 'expiration')
        .from('access')
        .where('token', token);
}

/**
 * Generates an access token for the given user ID.
 *
 * @param {Number} userId User ID
 * @returns {Bluebird<Object>} Token data
 */
async function generate(userId) {
    const token = await uid.generate();

    let id = await db
        .table('access')
        .insert({
            user: userId,
            token: token,
            expiration: Math.trunc((Date.now() + tokenExpiration) / 1000)
        });

    return db
        .first('*')
        .from('access')
        .where('id', id[0]);
}

module.exports = {
    find,
    generate
};
