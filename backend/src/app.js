const express = require('express');
const bodyParser = require('body-parser');
const log = require('./lib/logging');
const celebrate = require('celebrate');

const app = express();

// Production security
if (process.env.NODE_ENV === 'production') {
    app.enable('trust proxy');

    const helmet = require('helmet');
    app.use(helmet())
} else {
    // Dev logging
    const morgan = require('morgan');
    app.use(morgan('dev'));
}

// Request parsing
app.use(bodyParser.json({
    verify: (req, res, buf, encoding) => {
        // Save a raw body to the request object
        req.rawBody = buf.toString();
    }
}));

app.use('/', require('./routes'));

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    const err = new Error('Not Found');
    err.status = 404;
    next(err);
});

// error handlers
app.use(function (err, req, res, next) {
    let response = {
        message: err.message
    };

    // Only show error data if we're not in production
    if (req.app.get('env') !== 'production') {
        response.error = require('error-to-json')(err);
    }
    let status = err.status || 500;

    // Celebrate errors are all 400 bad request.
    if (celebrate.isCelebrate(err)) {
        status = 400;
    }

    log.error(err.message, {
        status: status,
        path: req.originalUrl,
        error: err.message,
        stack: err.stack
    });

    // render the error page
    res.status(status);
    res.json(response);
});

module.exports = app;
