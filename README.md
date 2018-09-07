# AM Wallet

This is a sample backend and mobile client for a token wallet built on API Miner.

There are two components:

* [backend](backend/) An Express.js API that handles user authentication and communication
with API Miner.
* [android](android/) An Android client for the backend with a simple wallet screen, and
the ability to transfer tokens.

See the respective READMEs of each component for more details.

## Customizing

Here are all the steps you'll need to take in order to have your own version of
the demo. For more details on each step, refer to the README of the corresponding
component.

### 1. Clone this repository

```
git clone https://github.com/novoa-media/apiminer-wallet.git
```

### 2. Backend prerequisites

For local development, you'll only need the API Miner access token and contract ID.

For production, you'll need Node.js hosting and a database. If using App Engine,
you'll need a [Google Cloud Platform](https://cloud.google.com/) account with a
[Cloud SQL](https://cloud.google.com/sql/docs/) instance.

### 3. Backend configuration

Add your API Miner access token and contract ID to `config.yaml`.

Add your database details to `knexfile.js`.

For *local development*, `cd` into `backend` and run the database migration:

```
npx knex-migrate up
```

For *production*, add the overriding config values to `app.yaml`.

### 4. Running the backend

Start the backend for either [local development](backend/README.md#starting_the_server)
or [production](backend/README.md#deployment).

Note the URL of the backend, as it's needed for the mobile app.

### 5. Android configuration

Change the backend URLs in `app/build.gradle` to match your backend.

You may wish to change the applicationId as well, so your version doesn't clash
with this demo.

### 6. Running Android

Use the standard Android deployment strategies to get the app on devices. For
local development, just use `./gradlew installDebug`.

### 7. Customize!

You should now have your own version of the demo running, so have at it.
