/**
 * Simple password hashing.
 */

/** */
const securePassword = require('secure-password');
const pwd = securePassword();

module.exports = {
    hash: Promise.promisify(pwd.hash, {context: pwd}),
    verify: Promise.promisify(pwd.verify, {context: pwd}),
    VALID: securePassword.VALID
};
