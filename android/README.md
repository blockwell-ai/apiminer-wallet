# apiminer-wallet android

A simple Android client for the apiminer-wallet demo.

Written in [Kotlin](https://kotlinlang.org/) using the following open source libraries:

* [Official Android support libraries](https://developer.android.com/topic/libraries/support-library/)
* Google's [Android KTX](https://developer.android.com/kotlin/ktx)
* Google's [Lifecycle ViewModel](https://developer.android.com/reference/android/arch/lifecycle/ViewModel)
* [google-gson](https://github.com/google/gson)
* [Anko](https://github.com/Kotlin/anko)
* [Kotpref](https://github.com/chibatching/Kotpref)
* [Koin](https://insert-koin.io/)
* [Stetho](http://facebook.github.io/stetho/)
* [QRGen](https://github.com/kenglxn/QRGen)
* [code-scanner](https://github.com/yuriy-budiyev/code-scanner)

## Configuration

### Backend URL

The API base URLs are defined in `app/build.gradle` for each build type. By default
the debug build points to `http://10.0.0.2:3000`, which you'll likely need to change.

## Building and running

All the commands are standard for Android:

```
./gradlew assembleDebug
```

```
./gradlew installDebug
```

## Release

To build a release, you'll need to configure the release signing config in `app/build.gradle`.
Beyond that, just use the standard `./gradlew assembleRelease`.
