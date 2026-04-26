package dev.seankim.composeremote.compositions

import androidx.compose.remote.creation.JvmRcPlatformServices
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.modifiers.RecordingModifier

private const val BRIEF_ACCENT = 0xFF0055FF.toInt()
private const val BRIEF_BACKGROUND = 0xFFFFFFFF.toInt()

/**
 * Minimal Brief variant: header + hero block.
 * Step 2 round-trip; we will flesh out the full seven cards once the loop is proven.
 */
fun briefDocument(): ByteArray {
    val ctx = RemoteComposeContext(
        390,
        844,
        "Brief - Daily",
        JvmRcPlatformServices(),
    ) {
        root {
            column(RecordingModifier().fillMaxWidth().padding(24).background(BRIEF_BACKGROUND)) {
                text("BRIEF")
                text("Today")
                text("3 new since yesterday")
                box(RecordingModifier().fillMaxWidth().height(180f).background(BRIEF_ACCENT))
                text("Featured release")
                text("A short summary of the headline release for today.")
            }
        }
    }
    // RemoteComposeWriter.buffer() returns the full 1 MiB backing array, not just
    // the bytes actually written. Trim to bufferSize() so we ship only the real document.
    return ctx.writer.buffer().copyOf(ctx.writer.bufferSize())
}
