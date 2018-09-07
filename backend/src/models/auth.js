/**
 * Authentication and registration model
 */

/** */
const config = require('../lib/config');
const users = require('../data/users');
const access = require('../data/access');
const hash = require('../lib/hash');
const error = require('../lib/error');
const log = require('../lib/logging');
const api = require('../apiminer');

/**
 * Registers a new user.
 *
 * @param {String} email User email address
 * @param {String} password User plaintext password
 * @returns {Promise<{user: Object, token: Object}>}
 */
async function register(email, password) {
    // Hash their password
    const hashed = await hash.hash(Buffer.from(password));

    // Create the user and access token
    let user = await users.create(email, hashed.toString('hex'));
    let token = await access.generate(user.id);

    // Load the API Miner client for the admin account
    const admin = api.getAdminClient();

    // Create an account,
    user.apiminerId = await admin.createUser(user.id.toString());
    user.apiminerToken = await admin.authorize(user.apiminerId);

    // Save the ID and access token
    await users.update(user);

    // Now that the user has an access token, we can use that for most calls
    const client = await api.getClient(user);

    // Create an account, ie. wallet
    user.account = await client.createAccount();

    // Save the user's account
    await users.update(user);

    // Finally, send the user some tokens, but don't wait for it to finish.
    // Note the lack of await.
    admin.transfer(user.account, config.get('initial_airdrop_amount'))
        .catch(err => log.error('Error with initial airdrop', {
            error: err.name,
            message: err.message,
            stack: err.stack
        }));

    return {user, token};
}

/**
 * Log the user in, generating a new access token.
 *
 * @param {String} email User email address
 * @param {String} password User plaintext password
 * @returns {Promise<Object>} Access token data
 */
async function login(email, password) {
    const user = await users.find(email);
    if (!user) {
        throw error(401, 'Login failed');
    }

    const verify = await hash.verify(Buffer.from(password), Buffer.from(user.password, 'hex'));

    if (verify === hash.VALID) {
        return access.generate(user.id);
    } else {
        throw error(401, 'Login failed');
    }
}

/**
 * Authenticate the given access token.
 *
 * @param {String} token Access token
 * @returns {Promise<Object>} User data
 */
async function authenticate(token) {
    const row = await access.find(token);

    if (!row) {
        throw error(401, 'Invalid access token');
    } else if (row.expiration < (Date.now() / 1000)) {
        throw error(401, 'Access token expired');
    } else {
        return users.get(row.user);
    }
}

/**
 * Connect-style middleware for authentication.
 *
 * This adds the user data to `req.user` for use in routes.
 *
 * @param {Request} req
 * @param {Response} res
 * @param {function} next
 */
function middleware(req, res, next) {
    const header = req.get('Authorization');

    if (!header) {
        throw error(403, 'Missing authorization');
    }

    const parts = header.trim().split(' ');

    if (parts.length !== 2 || parts[0].toLowerCase() !== "bearer") {
        throw error(400, 'Bad authorization header');
    }

    const token = parts[1];

    authenticate(token)
        .then(user => {
            req.user = user;
            next();
        })
        .catch(err => next(err));
}

module.exports = {
    register,
    login,
    authenticate,
    middleware
};
