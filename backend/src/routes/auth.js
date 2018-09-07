/**
 * Authentication endpoints, under '/auth'.
 */

const express = require('express');
const {celebrate} = require('celebrate');
const Joi = require('joi');
const router = express.Router();

const error = require('../lib/error');
const users = require('../data/users');
const auth = require('../models/auth');

router.post('/register', celebrate({
    body: Joi.object().keys({
        email: Joi.string().email().required(),
        password: Joi.string().min(4).max(128).required()
    })
}), (req, res, next) => {
    users.find(req.body.email)
        .then(user => {
            if (user) {
                throw error(409, 'User already exists');
            }

            return auth.register(req.body.email, req.body.password);
        })
        .then(result => {
            res.status(201).json({
                token: result.token.token,
                expiration: result.token.expiration
            });
        })
        .catch(next);
});

router.post('/login', celebrate({
    body: Joi.object().keys({
        email: Joi.string().email().required(),
        password: Joi.string().min(4).max(128).required()
    })
}), (req, res, next) => {
    auth.login(req.body.email, req.body.password)
        .then(result => {
            res.status(201).json({
                token: result.token,
                expiration: result.expiration
            });
        })
        .catch(next);
});

module.exports = router;
