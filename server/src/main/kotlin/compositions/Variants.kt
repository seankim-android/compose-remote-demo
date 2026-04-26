package dev.seankim.composeremote.compositions

import androidx.compose.remote.core.RcProfiles
import androidx.compose.remote.creation.JvmRcPlatformServices
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.actions.HostAction
import androidx.compose.remote.creation.modifiers.RecordingModifier

private const val BRIEF_ACCENT = 0xFF0055FF.toInt()
private const val BRIEF_BACKGROUND = 0xFFFFFFFF.toInt()
private const val CTA_PRIMARY = 0xFF0F172A.toInt()
private const val CTA_TONAL = 0xFFE2E8F0.toInt()

// Action ids form the contract between server and client.
// 1xxx = in-app intent. 2xxx = navigate to item N (id - 2000). 3xxx = detail intent.
object Actions {
    const val READ_FEATURED = 1001
    const val SAVE_FOR_LATER = 1002
    const val CATCH_ME_UP = 1003
    const val REFRESH = 1004
    const val MARK_READ = 3001
    const val OPEN_IN_SOURCE = 3002
    fun navigateItem(index: Int) = 2000 + index
}

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

private fun RemoteComposeContext.cta(label: String, actionId: Int, color: Int = CTA_PRIMARY) {
    box(
        RecordingModifier()
            .fillMaxWidth()
            .height(48f)
            .background(color)
            .padding(12)
            .onClick(HostAction(actionId)),
    ) {
        text(label)
    }
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
            cta("Read featured", Actions.READ_FEATURED)
            cta("Save for later", Actions.SAVE_FOR_LATER, color = CTA_TONAL)
        }
    }
}

fun sparseDocument(): ByteArray = render("Brief - Sparse") {
    root {
        column(RecordingModifier().fillMaxWidth().padding(24).background(BRIEF_BACKGROUND)) {
            text("BRIEF")
            text("Quiet day")
            text("Nothing new since yesterday.")
            cta("Catch me up", Actions.CATCH_ME_UP, color = CTA_TONAL)
        }
    }
}

fun catalogDocument(): ByteArray = render("Brief - Catalog") {
    root {
        column(RecordingModifier().fillMaxWidth().padding(24).background(BRIEF_BACKGROUND)) {
            text("BRIEF")
            text("Catalog")
            repeat(6) { i ->
                val n = i + 1
                box(RecordingModifier().fillMaxWidth().height(8f).background(BRIEF_ACCENT))
                box(
                    RecordingModifier()
                        .fillMaxWidth()
                        .padding(8)
                        .onClick(HostAction(Actions.navigateItem(n))),
                ) {
                    column(RecordingModifier().fillMaxWidth()) {
                        text("Item $n")
                        text("Short note for item $n.")
                    }
                }
            }
            cta("Refresh", Actions.REFRESH, color = CTA_TONAL)
        }
    }
}

fun itemDocument(id: Int): ByteArray = render("Brief - Item $id") {
    root {
        column(RecordingModifier().fillMaxWidth().padding(24).background(BRIEF_BACKGROUND)) {
            text("RELEASES")
            text("Item $id")
            text("Headline for item $id, written long enough to wrap onto two lines.")
            text("Sean Kim, today")
            text("Body paragraph one. The article body would normally live here, drawn from a CMS or stitched from a feed.")
            text("Body paragraph two. The point of having a server-emitted detail screen is that the layout is owned end to end by the server.")
            cta("Mark read", Actions.MARK_READ)
            cta("Open in source", Actions.OPEN_IN_SOURCE, color = CTA_TONAL)
        }
    }
}
