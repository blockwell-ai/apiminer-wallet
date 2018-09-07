/**
 * Database abstraction layer for Users.
 */

/**
 * @type {Knex}
 */
const db = require('../lib/db');

/**
 * Base function for getting a user by ID.
 *
 * @param {Array} fields Fields to select
 * @param {Number} id User ID
 * @returns {Bluebird<Object>}
 * @private
 */
function _get(fields, id) {
    return db
        .first(fields)
        .from('users')
        .where('id', id);
}

/**
 * Get a user by ID.
 *
 * @param {Number} id User ID
 * @returns {Bluebird<Object>} User data
 */
function get(id) {
    return _get(['id', 'email', 'account', 'apiminerId', 'created_at'], id);
}

/**
 * Get the user's hashed password.
 *
 * @param {Number} id User ID
 * @returns {Bluebird<String>} Hashed password
 */
function getPassword(id) {
    return _get(['password'], id).then(it => it.password);
}

/**
 * Get the user's API Miner token.
 *
 * @param {Number} id User ID
 * @returns {Bluebird<String>} API Miner token
 */
function getApiminerToken(id) {
    return _get(['apiminerToken'], id).then(it => it.apiminerToken);
}

/**
 * Find a user by email address.
 *
 * @param {String} email User email address
 * @returns {Bluebird<{id: Number, password: String}>}
 */
function find(email) {
    return db
        .first('id', 'password', 'account')
        .from('users')
        .where('email', email);
}

/**
 * Creates a user in the database.
 *
 * @param {String} email User email
 * @param {String} password Hashed password
 * @returns {Bluebird<Object>}
 */
function create(email, password) {
    return db
        .table('users')
        .insert({
            email: email,
            password: password
        })
        .then(id => {
            return get(id[0]);
        });
}

/**
 * Updates the user in the database.
 *
 * The user parameter must have its `id` set already.
 *
 * @param {Object} user User data
 * @returns {Bluebird<Boolean>} Resolves true when successful
 */
function update(user) {
    return db
        .table('users')
        .where('id', user.id)
        .update(user)
        .return(true);
}

module.exports = {
    get,
    getPassword,
    getApiminerToken,
    find,
    create,
    update
};
