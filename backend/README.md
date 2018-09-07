# apiminer-wallet backend

A simple Express.js backend for the apiminer-wallet demo.

Uses API Miner to manage user accounts/wallets, balances, and transfers.

# Setup

## Prerequisites

### API Miner

For API Miner, you'll need the following:

* User and API key
* Token contract ID

### App Engine

The demo is intended to be run on App Engine's new Node.js standard
environment, so you will need a 
[Google Cloud Platform](https://cloud.google.com/) account.

If you'd like to avoid using App Engine, see 
[Different Hosting](#different-hosting) below. The rest of this
document assumes the use of App Engine.

### Database

[Knex](https://knexjs.org/) is used for interacting with the database,
so any database supported by Knex should work:

> Postgres, MSSQL, MySQL, MariaDB, SQLite3, Oracle, and Amazon Redshift

The demo has been tested with MySQL and SQLite. When using App Engine, 
Cloud SQL is the easiest choice.

Note that database configuration is separate from the rest of the config,
in `knexfile.js`, to enable Knex's command line tools.

It's also worth noting that while SQLite works well for local development,
it cannot be used on App Engine, since the file isn't persisted or shared.

## Configuration

There are two primary files for configuring the application:

* `config.yaml` is the primary configuration file that contains all
different config options.
* `app.yaml` is the App Engine configuration.

Configuration is handled by [nconf](https://www.npmjs.com/package/nconf),
which uses a hierarchical configuration. Echo is set up so that
environment variables override what's in `config.yaml`, so that different
environments can easily use different values.

In practice this means the following:

* `config.yaml` itself is used mostly for *development* values.
* `app.yaml` contains `env_variables` that will override certain
values in *production* through the environment variables mechanism.

*Database* configuration is separately in `knexfile.js`, so the Knex
command line tools can be used.

Start by copying the example files:

```
cp config.example.yaml config.yaml
cp app.example.yaml app.yaml
cp knexfile.example.js knexfile.js
```

Then edit as necessary.

### `apiminer_url`

The URL of the API Miner environment. The default is for the Debug
environment that gives you more helpful error messages.

### `apiminer_token`

Set this to the API Token for your API Miner account.

### `token_contract_id`

The ID of the token contract in API Miner that should be used.

# Development

Use `npm install` to get Node.js dependencies as usual.

Create the SQLite database using Knex:

```
npx knex-migrate up
```

### Starting the server

Use `npm run dev` to start the server with automatic reloading.


# Deployment

Deployment to App Engine is done using the standard `gcloud` command:

```
gcloud app deploy
```

# Other notes

### Different Hosting

The sample is intended for App Engine, but it's straight-forward to modify
it to run on a different platform. Here are things you'll need to change.

#### Environment configuration

App Engine adds the production environment variables to the running
application from `app.yaml`. You'll need some mechanism to provide these
environment variables.

#### Trust proxy

Since the app is designed to be deployed on App Engine, in production
Express is configured to trust the proxy headers. IMPORTANT: If
deployed elsewhere, make sure to adjust this accordingly!
