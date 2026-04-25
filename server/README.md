# server/

Ktor service that emits remote compositions over HTTP.

## Bootstrap (first time)

1. Generate a Ktor project at https://start.ktor.io with:
   - Engine: Netty
   - Build: Gradle Kotlin DSL
   - Plugins: Routing, ContentNegotiation, kotlinx.serialization
2. Drop the generated files into this directory.
3. Add the Compose Remote authoring artifacts per the [AndroidX release notes](https://developer.android.com/jetpack/androidx/releases/compose-remote).

## Run

```bash
./gradlew run
```

Default port: `8080`.

## Routes (planned)

- `GET /screens/home` → serialized remote composition for the home screen.
- `GET /screens/:id` → arbitrary screen by id.

Update this section once the routes exist.
