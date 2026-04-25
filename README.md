# compose-remote-starter

A starter for experimenting with [Jetpack Compose Remote](https://developer.android.com/jetpack/androidx/releases/compose-remote): a tiny server that emits a remote composition, an Android host app that renders it, and a basic hot-reload loop so you can iterate on layout without rebuilding the app.

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

### 1. Run the server

```bash
cd server
./gradlew run
# Server listens on http://localhost:8080
```

### 2. Run the Android app

Open `android/` in Android Studio and run the app on an emulator or device. The app expects the server at `http://10.0.2.2:8080` (emulator → host).

### 3. Edit the layout

Change the composition emitted by `server/src/main/kotlin/Routes.kt` and re-request from the app. No rebuild needed on the client.

## Bootstrapping (first time)

The Gradle/Android scaffolding is intentionally not pre-generated, so versions stay current. Bootstrap once with:

- **Server:** [Ktor project generator](https://start.ktor.io) → drop output into `server/`.
- **Android:** Android Studio → New Project → Empty Compose Activity → target `android/`.
- **Shared (optional):** `gradle init` → Kotlin library → `shared/`.

Then add the AndroidX Compose Remote dependencies per the [release notes](https://developer.android.com/jetpack/androidx/releases/compose-remote).

## Why this exists

Most existing SSUI playbooks lean on a JSON schema and a custom renderer per platform. Compose Remote skips the renderer: the server emits Compose UI, the Compose runtime on the client renders it. This repo is a place to feel out what that actually looks like in practice.

Companion blog post: _Compose Remote: rethinking Server-Driven UI on Android_ (link TBD).

## License

MIT. See [LICENSE](./LICENSE).
