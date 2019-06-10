/**
 * Simple password hashing.
 */

/** */

const bcrypt = require('bcrypt');

const saltRounds = 10;

function hash(data) {
    return Promise.resolve(bcrypt.hash(data, saltRounds));
}

function verify(data, hash) {
    return Promise.resolve(bcrypt.compare(data, hash));
}

module.exports = {
    hash,
    verify
};
