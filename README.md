# compose-remote-starter

A starter for poking at [Jetpack Compose Remote](https://developer.android.com/jetpack/androidx/releases/compose-remote). Tiny Ktor server emits a remote composition, an Android app fetches and renders it, and you can edit the layout server-side without rebuilding the client.

> Status: **scaffold**. Compose Remote is at `1.0.0-alpha05`. APIs will move. This repo will move with them.

## Layout

```
compose-remote-starter/
├── server/         # Ktor server. Emits remote compositions over HTTP.
├── android/        # Host Android app. Fetches and renders.
├── shared/         # Types/schemas shared between server and client (optional).
├── samples/
│   └── basic/      # Minimal end-to-end demo using the template.
└── docs/           # Notes, diagrams, and screenshots.
```

The template (`server/`, `android/`, `shared/`) is what you fork or copy. `samples/basic/` is a runnable example so you can see the wiring before you start your own project.

## Quick start

```bash
./bootstrap.sh init     # asks a couple questions, points you at the generators
# (run the Ktor + Android Studio steps it prints)
./bootstrap.sh verify   # checks both sides are scaffolded
./bootstrap.sh run-server
```

Then open `android/` in Android Studio and Run. The app hits the server at `http://10.0.2.2:8080` from the emulator.

To iterate on layout: edit the composition in `server/` (e.g. `Routes.kt`) and re-request from the app. No client rebuild needed.

## Why a script and not a pre-baked scaffold

The Gradle/Android scaffolding isn't checked in on purpose. Compose Remote is `1.0.0-alpha05`, Ktor and AGP move on their own schedules, and pinned templates rot fast. `bootstrap.sh` drives the upstream generators ([start.ktor.io](https://start.ktor.io), Android Studio's New Project flow) so you get current versions, then verifies the result.

After scaffolding, add the AndroidX Compose Remote dependencies per the [release notes](https://developer.android.com/jetpack/androidx/releases/compose-remote).

## Why this exists

Most server-driven UI setups ship a JSON schema and a hand-written renderer per platform. Compose Remote drops the renderer: the server emits Compose UI, the Compose runtime on the device renders it. I wanted somewhere to actually try that and see where it falls over.

More background: [Compose Remote: rethinking Server-Driven UI on Android](https://seankim.dev/blog/compose-remote-rethinking-server-driven-ui-on-android/).

## License

MIT. See [LICENSE](./LICENSE).
