# AM Wallet

This is a sample backend and mobile client for a token wallet built on API Miner.

There are two components:

* [backend](backend/) An Express.js API that handles user authentication and communication
with API Miner.
* [android](android/) An Android client for the backend with a simple wallet screen, and
the ability to transfer tokens.

See the respective READMEs of each component for more details.

## Local development quickstart

1. Install [Node.js LTS](https://nodejs.org/) and [Android Studio](https://developer.android.com/studio).

2. Clone this repository:

```
git clone https://github.com/blockwell-ai/apiminer-wallet.git
cd apiminer-wallet
```

3. Configure backend:

```
cd backend
cp config.example.yaml config.yaml
cp app.example.yaml app.yaml
cp knexfile.example.js knexfile.js
```

4. Edit config.yaml and put in your `apiminer_token` and `token_contract_id`.
5. Install dependencies and initialize the database:

```
npm install
npx knex-migrate up
```

6. Then run the development server:

```
npx run dev
```

Keep this running.

7. To configure the mobile app, you'll need the IP address of the server running
on your computer.

- If you're using an Android emulator, this IP address is `10.0.2.2`.
- If using a real phone, both the computer and your phone need to be on the same
local network, and you need to use your computer's IP address on the network. You
can try getting the address using `ifconfig | grep -Po 'inet \K[\d.]+'` - the correct
one is probably the one that starts with either `10.0.` or `192.168.`.

8. Open Android Studio.

If this is the first time you're running Android Studio:

- First select Do not import settings.
- In the Wizard:
	- Click Next
	- Select Custom and click Next
	- Choose your color scheme of choice and click Next
	- Make sure the SDK Platform listed greyed out is API 28: ANdroid 9.0 (Pie)
	- If you want to use an Android Emulator, check Android Virtual Device
	- Click Next, Next and Finish
	- Android Studio will then download the necessary components, then click
	Finish again.

Click "Open an existing Android Studio project", select the `android` folder
in this repository, and click OK.

You may see an error like this:

```
ERROR: Failed to install the following Android SDK packages as some licences have not been accepted.
```

At the bottom of the message there's a link for "Instal missing SDK package(s)",
click that. Accept any licenses needed, and go through the wizard.

Android Studio may say "Android Gradle Plugin Update Recommended". Click
"Don't remind me again for this project".

9. Edit the file `build.gradle (Module: app)` under Gradle Scripts, and 
replace the IP address `10.0.0.2` with your server IP address from Step 7.

10. Connect your Android device or start an emulator.

If using an emulator, you can start one by going to Tools -> AVD Manager,
and clicking on the Play button on the right of the listed emulator.

11. Run the app

Go to Run -> Run 'app', and a window listing your device or emulator
should show. Click OK.

The app will then build, be installed on the device, and be started.

If you see an error installing the app, try doing Build -> Rebuild Project,
and running again.

## Customizing

Here are all the steps you'll need to take in order to have your own version of
the demo. For more details on each step, refer to the README of the corresponding
component.

### 1. Clone this repository

```
git clone https://github.com/blockwell-ai/apiminer-wallet.git
```

### 2. Backend prerequisites

For local development, you'll only need the API Miner access token and contract ID.

For production, you'll need Node.js hosting and a database. If using App Engine,
you'll need a [Google Cloud Platform](https://cloud.google.com/) account with a
[Cloud SQL](https://cloud.google.com/sql/docs/) instance.

### 3. Backend configuration

See detailed Setup in [backend](backend/).

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
