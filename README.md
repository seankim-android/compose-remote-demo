# compose-remote-demo

A small runnable demo of [Jetpack Compose Remote](https://developer.android.com/jetpack/androidx/releases/compose-remote). A Ktor server emits a remote composition, an Android client fetches and renders it, and you can edit the layout server-side without rebuilding the client.

> Status: alpha. Compose Remote is at `1.0.0-alpha05`. APIs will move; this repo will move with them.

## Layout

```
compose-remote-demo/
├── server/    # Ktor server. Emits remote compositions over HTTP.
├── android/   # Android client. Fetches and renders.
├── shared/    # Optional shared types/schemas.
└── docs/      # Notes, diagrams, screenshots.
```

## Run it

```bash
cd server && ./gradlew run
```

Then open `android/` in Android Studio and Run. The app hits the server at `http://10.0.2.2:8080` from the emulator.

To iterate on layout: edit the composition in `server/` (e.g. `Routes.kt`) and re-request from the app. No client rebuild needed.

## Fork it as a starter

If you'd rather start clean with current Ktor/AGP versions instead of the ones pinned here, run:

```bash
./bootstrap.sh
```

It walks you through generating fresh Ktor + Android Studio scaffolds into `server/` and `android/`. The reason it isn't pre-baked: Compose Remote is alpha, Ktor and AGP move on their own schedules, and pinned templates rot.

After scaffolding, add the AndroidX Compose Remote dependencies per the [release notes](https://developer.android.com/jetpack/androidx/releases/compose-remote).

## Why this exists

Most server-driven UI setups ship a JSON schema and a hand-written renderer per platform. Compose Remote drops the renderer: the server emits Compose UI, the Compose runtime on the device renders it. I wanted somewhere to actually try that and see where it falls over.

More background: [Compose Remote: rethinking Server-Driven UI on Android](https://seankim.dev/blog/compose-remote-rethinking-server-driven-ui-on-android/).

## License

MIT. See [LICENSE](./LICENSE).
