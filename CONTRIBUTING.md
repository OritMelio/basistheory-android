# Contributing

## Prerequisites

First ensure you have the JDK installed:

```shell
brew install openjdk@17
```

Next, ensure you have the Android SDK installed, which can be installed with [Android Studio](https://developer.android.com/about/versions/12/setup-sdk).
You will need to have the `adb` CLI available in your PATH, which can be done after installing
Android Studio by adding the following to your `~/.zshrc` or `~/.bash_profile`:

```shell
export PATH=~/Library/Android/sdk/tools:$PATH
export PATH=~/Library/Android/sdk/platform-tools:$PATH
```

### Set Build Configuration

Copy the `./local.properties.example` file to `./local.properties` and replace the values:

- `BASIS_THEORY_API_URL`: Either the dev or production Basis Theory API URL
- `BASIS_THEORY_API_KEY`: A Basis Theory API key with `token:create` permission on at least the `/general/` container

This will inject these configuration settings at build time into the example app. These settings 
be available through the `BuildConfig` class within the app code.

### Build the SDK and Install Dependencies

The following command builds both the example app and SDK library:

```shell
./gradlew build -x test
```

Note: The command `./gradlew build` is also valid, but it runs all unit tests too.

## Running tests

You can run all tests locally by simply executing the command:

```shell
make verify
```

For more fine-grained control when running test suites, see the sections below.

### Unit Tests

Unit tests are written against the `lib` module using [Robolectric](https://robolectric.org/). 
To run unit tests, execute the command:

```shell
./gradlew test
```

### Acceptance Tests

Acceptance tetss rely upon the `example` module to provide an Android app that consumes the `lib`
module, simulating a real consumer of our Android Library. Acceptance tests are written using 
[Espresso](https://developer.android.com/training/testing/espresso) and rely upon an emulator being 
configured and running on your local machine. An emulator should come pre-installed with 
Android Studio, or you can follow [these instructions](https://developer.android.com/studio/run/managing-avds) 
to set up a new virtual device to run on your machine.

Once the emulator is booted and available, you can run Espresso tests through Android Studio or by
executing the command:

```shell
./gradlew connectedCheck
```

## Troubleshooting

If you receive a 400 error while tokenizing within the example app due to `'expires_at' must be a future datetime`,
it is likely due to skew between your local system clock and the Android emulator. To resync the clock
on the emulator with your local system, run:

```shell
make emulator-sync-clock
```