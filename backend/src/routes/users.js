/**
 * User endpoints, under '/users'.
 */

const express = require('express');
const router = express.Router();
const auth = require('../models/auth');

// Require Authentication for all routes here
router.use(auth.middleware);

router.get('/me', (req, res) => {
    res.json(req.user);
});

module.exports = router;
