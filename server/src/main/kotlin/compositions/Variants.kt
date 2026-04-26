package dev.seankim.composeremote.compositions

import androidx.compose.remote.core.RcProfiles
import androidx.compose.remote.creation.JvmRcPlatformServices
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.modifiers.RecordingModifier

private const val BRIEF_ACCENT = 0xFF0055FF.toInt()
private const val BRIEF_BACKGROUND = 0xFFFFFFFF.toInt()

enum class Variant {
    BRIEF, SPARSE, CATALOG;

    companion object {
        fun parse(raw: String?): Variant = when (raw?.lowercase()) {
            "sparse" -> SPARSE
            "catalog" -> CATALOG
            else -> BRIEF
        }
    }
}

fun documentFor(variant: Variant): ByteArray = when (variant) {
    Variant.BRIEF -> briefDocument()
    Variant.SPARSE -> sparseDocument()
    Variant.CATALOG -> catalogDocument()
}

private fun render(name: String, body: RemoteComposeContext.() -> Unit): ByteArray {
    val ctx = RemoteComposeContext(
        390,
        844,
        name,
        6,
        RcProfiles.PROFILE_ANDROIDX,
        JvmRcPlatformServices(),
        body,
    )
    // RemoteComposeWriter.buffer() returns the full backing array, not just
    // the bytes written. Trim to bufferSize() so we ship only the real document.
    return ctx.writer.buffer().copyOf(ctx.writer.bufferSize())
}

fun briefDocument(): ByteArray = render("Brief - Daily") {
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

fun sparseDocument(): ByteArray = render("Brief - Sparse") {
    root {
        column(RecordingModifier().fillMaxWidth().padding(24).background(BRIEF_BACKGROUND)) {
            text("BRIEF")
            text("Quiet day")
            text("Nothing new since yesterday.")
        }
    }
}

fun catalogDocument(): ByteArray = render("Brief - Catalog") {
    root {
        column(RecordingModifier().fillMaxWidth().padding(24).background(BRIEF_BACKGROUND)) {
            text("BRIEF")
            text("Catalog")
            repeat(6) { i ->
                box(RecordingModifier().fillMaxWidth().height(8f).background(BRIEF_ACCENT))
                text("Item ${i + 1}")
                text("Short note for item ${i + 1}.")
            }
        }
    }
}
