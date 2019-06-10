const config = require('../lib/config');

// This is the ID of the Token we're using
const contractId = config.get('token_contract_id');

/**
 * Simple client for API Miner.
 */
class ApiMiner {
    constructor(accessToken, rp) {
        this.accessToken = accessToken;

        this.rp = rp.defaults({
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        })
    }

    /**
     * Make a request.
     *
     * @param {Object} data Request data passed to request
     * @returns {Promise<Object>} API response
     */
    request(data) {
        return this.rp(data);
    }

    /**
     * Create a new API Miner user.
     *
     * The `userId` passed will be associated with the user in the API Miner
     * system as their `externalId`.
     *
     * @param {String} userId Local user ID
     * @returns {Promise<String>} API Miner user ID
     */
    createUser(userId) {
        return this.request({
            method: 'POST',
            uri: `/users`,
            body: {
                externalId: userId
            }
        }).then(response => response.data.id);
    }

    /**
     * Generates a new access token for the given user.
     *
     * This needs to be called as either the main system user,
     * or the specific user who needs the new token.
     *
     * @param {String} apiminerId User API Miner ID
     * @returns {Promise<String>} New access token
     */
    authorize(apiminerId) {
        return this.request({
            method: 'POST',
            uri: `/users/${apiminerId}/authenticate`,
        }).then(response => response.data);
    }

    /**
     * Create a new Ethereum account (aka. wallet).
     *
     * @returns {Promise<String>} Address of the new account
     */
    createAccount() {
        return this.request({
            method: 'POST',
            uri: `/accounts`,
            body: {
                default: true
            }
        }).then(response => response.data.address);
    }

    /**
     * Transfer Tokens.
     *
     * Uses the user's default account as the from address.
     *
     * @param {String} to Address to transfer to
     * @param {String} amount Amount of tokens to transfer
     * @returns {Promise<String>} Transaction ID
     *
     */
    transfer(to, amount) {
        return this.request({
            method: 'POST',
            uri: `/tokens/${contractId}/transfers`,
            body: {
                to: to,
                value: amount
            }
        }).then(response => response.data.id);
    }

    transferEther(to, amount, network) {
        return this.request({
            method: 'POST',
            uri: `/transactions`,
            body: {
                to: to,
                value: amount,
                network: network
            }
        }).then(response => response.data.id);
    }

    /**
     * Get the status of a transfer by its transaction ID.
     *
     * @param {String} transactionId Transaction ID
     * @returns {Promise<Object>} Transaction data
     */
    getTransaction(transactionId) {
        return this.request({
            uri: `/transactions/${transactionId}`
        }).then(response => response.data);
    }

    /**
     * Gets all transfers for the given account address.
     *
     * @param {String} account Account address
     * @returns {Promise<Array<Object>>} List of transfers
     */
    getTransfers(account) {
        return this.request({
            uri: `/tokens/${contractId}/transfers`,
            qs: {
                address: account
            }
        }).then(response => response.data);
    }

    /**
     * Gets the balance for the user's account.
     *
     * @returns {Promise<String>} Account balance
     */
    getBalance() {
        return this.request({
            uri: `/tokens/${contractId}/balances/default`,
        }).then(response => response.data);
    }

    getContract(id) {
        return this.request({
            uri: `/contracts/${contractId}`,
        }).then(response => response.data);
    }
}

module.exports = ApiMiner;
