# Remote Compose research notes

Captured during initial setup. Source: AndroidX release page + sobaya-0141/RemoteComposeSample.
Treat as best-known surface area; alpha churn likely.

## Latest published artifacts (alpha09)

```
androidx.compose.remote:remote-core:1.0.0-alpha09
androidx.compose.remote:remote-creation:1.0.0-alpha09
androidx.compose.remote:remote-creation-core:1.0.0-alpha09
androidx.compose.remote:remote-creation-android:1.0.0-alpha09
androidx.compose.remote:remote-creation-jvm:1.0.0-alpha09
androidx.compose.remote:remote-creation-compose:1.0.0-alpha09
androidx.compose.remote:remote-player-core:1.0.0-alpha09
androidx.compose.remote:remote-player-view:1.0.0-alpha09
androidx.compose.remote:remote-tooling-preview:1.0.0-alpha09
```

`remote-creation-jvm` is the artifact we need on the Ktor server (no Android dependency).
`remote-player-compose` is what the Android client uses to render.

## Authoring API (from sobaya sample, alpha01 — names likely stable)

Imports:

```kotlin
import androidx.compose.remote.creation.RemoteComposeContextAndroid
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.platform.AndroidxRcPlatformServices
import androidx.compose.remote.creation.modifiers.RecordingModifier
import androidx.compose.remote.creation.actions.HostAction
import androidx.compose.remote.core.RcProfiles
import androidx.compose.remote.core.operations.layout.managers.BoxLayout
```

Shape:

```kotlin
RemoteComposeContextAndroid(
    width = 400,
    height = 400,
    contentDescription = "Simple",
    apiLevel = 6,
    profiles = RcProfiles.PROFILE_ANDROIDX,
    platform = AndroidxRcPlatformServices(),
) {
    root {
        column(RecordingModifier().fillMaxSize()) {
            text(string = "SAMPLE")
            box(
                modifier = Modifier
                    .size(200, 200)
                    .background(0xFFDDDDDD.toInt())
                    .onClick(HostAction(1001))
            )
        }
    }
}.writer
```

Output bytes:

```kotlin
val bytes: ByteArray = writer.buffer.buffer.cloneBytes()
```

Open question: is there a `RemoteComposeContextJvm` or equivalent in `remote-creation-jvm`?
The `-jvm` artifact name suggests yes. Until verified, two paths forward:

- **Path A (preferred):** Server uses the JVM authoring context, emits bytes over HTTP.
- **Path B (fallback):** Client authors locally based on a server-supplied JSON spec or a server-supplied "variant" enum, then renders. Less pure SSUI but still demos the player.

## Player API (Android, alpha01)

Imports:

```kotlin
import androidx.compose.remote.player.compose.ExperimentalRemotePlayerApi
import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import androidx.compose.remote.player.core.RemoteDocument
```

Shape:

```kotlin
@OptIn(ExperimentalRemotePlayerApi::class)
@Composable
fun Player(bytes: ByteArray) {
    val doc = remember(bytes) { RemoteDocument(bytes) }
    RemoteDocumentPlayer(
        document = doc.document,
        documentWidth = 400,
        documentHeight = 400,
        debugMode = 0,
        onNamedAction = { _, _, _ -> },
        onAction = { id, metadata -> /* handle clicks */ },
    )
}
```

Note `ExperimentalRemotePlayerApi` opt-in is required.

## Modifier system

`RecordingModifier()` is the authoring-side equivalent of `Modifier`. Method names appear
to mirror Compose: `.fillMaxSize()`, `.size(w, h)`, `.background(intColor)`, `.onClick(...)`.
Actions are typed: `HostAction(id)` for client-side handling, looks like there is also a
`NamedAction` based on the player's `onNamedAction` callback signature.

## Caveats

- All API names captured here are from alpha01. Verify against alpha09 once we add deps and
  the IDE surfaces the actual symbols.
- The sample author + renders on the same Android app. We need to split: JVM author, Android render.
- `apiLevel = 6` and `RcProfiles.PROFILE_ANDROIDX` look like opinion knobs we'll re-examine.
- No documented serialization format guarantees yet; treat the byte array as opaque and only
  decode it on a player from the same alpha.

## Decisions for the demo

- Use `remote-creation-jvm` on the server. If that artifact lacks a JVM context, fall back
  to Path B above and document the limitation in the blog post.
- Use `remote-player-compose` on Android.
- Pin `1.0.0-alpha09` in a single `gradle/libs.versions.toml`-style version constant so the
  bump is one line later.

## References

- [AndroidX Remote Compose release page](https://developer.android.com/jetpack/androidx/releases/compose-remote)
- [sobaya-0141/RemoteComposeSample](https://github.com/sobaya-0141/RemoteComposeSample)
- [androidx/androidx compose/remote source tree](https://github.com/androidx/androidx/tree/androidx-main/compose/remote)
