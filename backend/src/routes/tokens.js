/**
 * Tokens endpoints, corresponds to `/tokens`.
 */

const express = require('express');
const router = express.Router();
const {celebrate} = require('celebrate');
const Joi = require('joi');
const users = require('../data/users');
const auth = require('../models/auth');
const api = require('../apiminer');
const error = require('../lib/error');

// Require Authentication for all routes here
router.use(auth.middleware);

router.get('/balance', (req, res, next) => {
    api.getClient(req.user)
        .then(client => client.getBalance())
        .then(balance => {
            res.json({account: req.user.account, balance: balance});
        })
        .catch(next);
});

router.get('/transfers', (req, res, next) => {
    api.getClient(req.user)
        .then(client => client.getTransfers(req.user.account))
        .then(transfers => {
            res.json(transfers);
        })
        .catch(next);
});

router.post('/transfers', celebrate({
    body: Joi.object().keys({
        // 'to' can be an email or an Ethereum address
        to: Joi.alt([
            Joi.string().email().required(),
            Joi.string().regex(/^0x[0-9a-f]{40}$/i)
        ]),
        value: Joi.string().regex(/^[0-9]+$/)
    })
}), (req, res, next) => {
    (async function () {
        const client = await api.getClient(req.user);
        const to = req.body.to;
        let address;

        if (to.includes('@')) {
            let recipient = await users.find(to);
            if (!recipient) {
                throw error(400, 'Recipient not found');
            }
            address = recipient.account;
        } else {
            address = to;
        }

        return client.transfer(address, req.body.value)
            .then(txId => {
                res.json({transactionId: txId});
            });
    })().catch(next);
});

router.get('/transactions/:id', celebrate({
    params: Joi.object().keys({
        id: Joi.string().guid()
    })
}), (req, res, next) => {
    api.getClient(req.user)
        .then(client => client.getTransaction(req.params.id))
        .then(txn => {
            res.json(txn);
        })
        .catch(next);
});

module.exports = router;
