# android/

Android client. Fetches a remote composition from `server/` and renders it with the Compose Remote runtime.

## Run

Open this folder in Android Studio and run on an emulator. The app talks to the local server at `http://10.0.2.2:8080` (the emulator's loopback to your host).

For a physical device on the same network, point `BASE_URL` at your machine's LAN IP.

## Structure

- `MainActivity`: entry point, Compose theme.
- `RemoteScreen` (planned): fetches a remote composition and hands it to the Compose Remote player.
- `network/` (planned): Ktor client wired to the server.

Package: `dev.seankim.composeremote.client`.
