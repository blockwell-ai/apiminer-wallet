/**
 * Creates a HTTP error.
 *
 * @param {Number} status HTTP status code
 * @param {String} message Error message
 * @returns {Error}
 */
module.exports = function (status, message) {
    let err = new Error(message);
    err.status = status;
    return err;
};
