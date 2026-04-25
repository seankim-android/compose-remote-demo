# android/

Host Android app. Fetches a remote composition from `server/` and renders it with the Compose Remote runtime.

## Bootstrap (first time)

1. Open Android Studio → New Project → **Empty Activity** (Compose).
2. Set package name (suggested: `dev.seankim.composeremote.starter`).
3. Drop the generated files into this directory.
4. Add the Compose Remote player artifacts per the [AndroidX release notes](https://developer.android.com/jetpack/androidx/releases/compose-remote).

## Run

Open the project root in Android Studio and run on an emulator. The app expects the local server at `http://10.0.2.2:8080` (emulator → host machine).

For a physical device on the same network, set `BASE_URL` to your machine's LAN IP.

## Structure (planned)

- `MainActivity` — entry point, Compose theme, navigation host.
- `RemoteScreen` — Composable that fetches a remote composition and hands it to the Compose Remote player.
- `network/` — Ktor client wired to the server.

Update this section once the code exists.
