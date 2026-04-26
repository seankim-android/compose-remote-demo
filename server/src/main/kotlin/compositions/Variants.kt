package dev.seankim.composeremote.compositions

import androidx.compose.remote.core.RcProfiles
import androidx.compose.remote.creation.JvmRcPlatformServices
import androidx.compose.remote.creation.RemoteComposeContext
import androidx.compose.remote.creation.actions.HostAction
import androidx.compose.remote.creation.modifiers.RecordingModifier
import androidx.compose.remote.creation.modifiers.RoundedRectShape

// Editorial palette. One accent, paper white, near-black ink.
private const val ACCENT = 0xFF0055FF.toInt()
private const val BG = 0xFFFFFFFF.toInt()
private const val INK_PRIMARY = 0xFF121212.toInt()
private const val INK_SECONDARY = 0xFF5F6368.toInt()
private const val INK_TERTIARY = 0xFF8A8F95.toInt()
private const val ON_ACCENT = 0xFFFFFFFF.toInt()
private const val TONAL = 0xFFECEEF1.toInt()

// Type families. Compose Remote resolves these against the player's Typeface
// lookup; "serif" / "sans-serif" / "monospace" are guaranteed to land somewhere
// reasonable. Custom families would need a downloadable font on the client.
private const val FAM_DISPLAY = "serif"
private const val FAM_BODY = "sans-serif"
private const val FAM_MONO = "monospace"

// Style ints from androidx.compose.remote.core.
private const val ITALIC = 1
private const val ALIGN_LEFT = 1
private const val ALIGN_CENTER = 3
private const val BOX_START = 1
private const val BOX_CENTER = 2
private const val BOX_END = 3
private const val BOX_TOP = 4
private const val BOX_BOTTOM = 5

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

fun documentFor(variant: Variant, viewport: Viewport): ByteArray = when (variant) {
    Variant.BRIEF -> briefDocument(viewport)
    Variant.SPARSE -> sparseDocument(viewport)
    Variant.CATALOG -> catalogDocument(viewport)
}

fun descriptorFor(variant: Variant): ScreenDescriptor = when (variant) {
    Variant.BRIEF -> ScreenDescriptor(
        name = "Brief - Daily",
        variant = variant.name.lowercase(),
        widthDp = 390,
        heightDp = 844,
        actions = listOf(
            ActionDescriptor(Actions.READ_FEATURED, "Read featured"),
            ActionDescriptor(Actions.SAVE_FOR_LATER, "Save for later"),
        ),
    )
    Variant.SPARSE -> ScreenDescriptor(
        name = "Brief - Sparse",
        variant = variant.name.lowercase(),
        widthDp = 390,
        heightDp = 844,
        actions = listOf(
            ActionDescriptor(Actions.CATCH_ME_UP, "Catch me up"),
        ),
    )
    Variant.CATALOG -> ScreenDescriptor(
        name = "Brief - Catalog",
        variant = variant.name.lowercase(),
        widthDp = 390,
        heightDp = 844,
        actions = (1..6).map { n -> ActionDescriptor(Actions.navigateItem(n), "Item $n") } +
            listOf(ActionDescriptor(Actions.REFRESH, "Refresh")),
    )
}

fun itemDescriptor(id: Int): ScreenDescriptor = ScreenDescriptor(
    name = "Brief - Item $id",
    id = id,
    widthDp = 390,
    heightDp = 844,
    actions = listOf(
        ActionDescriptor(Actions.MARK_READ, "Mark read"),
        ActionDescriptor(Actions.OPEN_IN_SOURCE, "Open in source"),
    ),
)

// Default canvas if the client did not pass its viewport. Roughly a Pixel 6
// content area (top bar + picker subtracted) so single-shot calls still look
// reasonable. With SIZING_LAYOUT the canvas is mostly a hint; the document
// lays out to fill the player's actual width.
private const val DEFAULT_W = 412
private const val DEFAULT_H = 760

// RootContentBehavior constants. Compose Remote stores `text(fontSize=N)` and
// modifier sizes as raw pixels. The player's default sizing mode is NONE, so
// the doc draws at 1:1 pixels — 18 in authored coords ends up as 18 px on
// screen, which is ~7sp on a 2.6x density device. That's the "everything looks
// tiny" trap. SIZING_SCALE + SCALE_FIT scales the doc canvas to the view,
// turning authored-pixel coords into device-pixel coords by the right factor.
private const val SCROLL_NONE = 0
private const val ALIGN_TOP_START = 17 // ALIGNMENT_TOP (1) | ALIGNMENT_START (16)
private const val SIZING_SCALE = 2
private const val SCALE_FIT = 4

// `density` is the client's display density (px per dp). We need it because
// Compose Remote's text() and modifier sizes are stored as raw pixels and
// passed straight to Paint.setTextSize / View dimensions. Without scaling by
// density, "fontSize = 18" renders at 18px which is ~7sp on a 2.6x device.
// Default 1.0f means "treat numbers as pixels," matching unscaled web tools.
data class Viewport(val w: Int, val h: Int, val density: Float) {
    companion object {
        fun parse(rawW: String?, rawH: String?, rawDensity: String? = null): Viewport {
            val w = rawW?.toIntOrNull()?.coerceIn(280, 1200) ?: DEFAULT_W
            val h = rawH?.toIntOrNull()?.coerceIn(400, 2400) ?: DEFAULT_H
            val d = rawDensity?.toFloatOrNull()?.coerceIn(0.5f, 5f) ?: 1f
            return Viewport(w, h, d)
        }
    }
}

private fun render(
    name: String,
    viewport: Viewport,
    body: RemoteComposeContext.() -> Unit,
): ByteArray {
    val ctx = RemoteComposeContext(
        viewport.w,
        viewport.h,
        name,
        6,
        RcProfiles.PROFILE_ANDROIDX,
        JvmRcPlatformServices(),
    ) {
        // Tell the player to lay out the doc to fill its actual size, not to
        // scale a fixed canvas to fit. Without this, authored sp values render
        // tiny on a phone-sized viewport because the player downscales.
        setRootContentBehavior(SCROLL_NONE, ALIGN_TOP_START, SIZING_SCALE, SCALE_FIT)
        body()
    }
    return ctx.writer.buffer().copyOf(ctx.writer.bufferSize())
}

private fun rounded(r: Float) = RoundedRectShape(r, r, r, r)

// ---- Type helpers ---------------------------------------------------------

private fun RemoteComposeContext.eyebrow(label: String) = text(
    string = label.uppercase(),
    color = INK_SECONDARY,
    fontSize = 12f,
    fontWeight = 500f,
    fontFamily = FAM_BODY,
    letterSpacing = 0.08f,
)

private fun RemoteComposeContext.section(label: String) = text(
    string = label,
    color = INK_PRIMARY,
    fontSize = 14f,
    fontWeight = 500f,
    fontFamily = FAM_BODY,
)

private fun RemoteComposeContext.meta(label: String) = text(
    string = label,
    color = INK_SECONDARY,
    fontSize = 14f,
    fontFamily = FAM_BODY,
)

private fun RemoteComposeContext.headline(label: String) = text(
    string = label,
    color = INK_PRIMARY,
    fontSize = 28f,
    fontStyle = ITALIC,
    fontWeight = 600f,
    fontFamily = FAM_DISPLAY,
    lineHeightAdd = 4f,
)

private fun RemoteComposeContext.deck(label: String) = text(
    string = label,
    color = INK_SECONDARY,
    fontSize = 16f,
    fontFamily = FAM_BODY,
    lineHeightAdd = 4f,
)

private fun RemoteComposeContext.bodyParagraph(label: String) = text(
    string = label,
    color = INK_PRIMARY,
    fontSize = 16f,
    fontFamily = FAM_DISPLAY,
    lineHeightAdd = 6f,
)

// ---- CTA helpers ----------------------------------------------------------

private fun RemoteComposeContext.primaryCta(label: String, actionId: Int) {
    box(
        modifier = RecordingModifier()
            .fillMaxWidth()
            .background(ACCENT)
            .clip(rounded(8f))
            .padding(20, 18, 20, 18)
            .onClick(HostAction(actionId)),
        horizontal = BOX_CENTER,
        vertical = BOX_CENTER,
    ) {
        text(
            string = "$label  ›",
            color = ON_ACCENT,
            fontSize = 18f,
            fontWeight = 600f,
            fontFamily = FAM_BODY,
            textAlign = ALIGN_CENTER,
        )
    }
}

// Right-aligned text button. Used for "Save for later", which the design renders
// as a quiet inline action rather than a full tonal fill.
private fun RemoteComposeContext.textCta(
    label: String,
    actionId: Int,
    align: Int = BOX_END,
) {
    box(
        modifier = RecordingModifier()
            .fillMaxWidth()
            .padding(8, 10, 8, 10)
            .onClick(HostAction(actionId)),
        horizontal = align,
        vertical = BOX_CENTER,
    ) {
        text(
            string = label,
            color = ACCENT,
            fontSize = 15f,
            fontWeight = 500f,
            fontFamily = FAM_BODY,
        )
    }
}

private fun RemoteComposeContext.hero(label: String) {
    box(
        modifier = RecordingModifier()
            .fillMaxWidth()
            .height(200f)
            .background(ACCENT)
            .clip(rounded(12f)),
        horizontal = BOX_START,
        vertical = BOX_BOTTOM,
    ) {
        column(
            modifier = RecordingModifier().fillMaxWidth().padding(16),
            horizontal = BOX_START,
            vertical = BOX_BOTTOM,
        ) {
            text(
                string = label,
                color = ON_ACCENT,
                fontSize = 22f,
                fontStyle = ITALIC,
                fontWeight = 600f,
                fontFamily = FAM_DISPLAY,
            )
        }
    }
}

// ---- Documents ------------------------------------------------------------

private fun pageColumn() = RecordingModifier()
    .fillMaxSize()
    .background(BG)
    .padding(20, 12, 20, 24)
    .spacedBy(16f)

fun briefDocument(viewport: Viewport): ByteArray = render("Brief - Daily", viewport) {
    root {
        column(modifier = pageColumn()) {
            section("Today")
            meta("3 new since yesterday")
            hero("Featured release")
            headline("Featured release")
            deck("A short summary of the headline release for today.")
            primaryCta("Read featured", Actions.READ_FEATURED)
            textCta("Save for later", Actions.SAVE_FOR_LATER)
        }
    }
}

fun sparseDocument(viewport: Viewport): ByteArray = render("Brief - Sparse", viewport) {
    root {
        column(modifier = pageColumn()) {
            headline("Quiet day")
            deck("Nothing new since yesterday.")
            textCta("Catch me up", Actions.CATCH_ME_UP, align = BOX_CENTER)
        }
    }
}

fun catalogDocument(viewport: Viewport): ByteArray = render("Brief - Catalog", viewport) {
    root {
        column(modifier = pageColumn()) {
            section("Catalog")
            val titles = listOf(
                "Featured release" to "Headline release for today.",
                "Notes from a quiet wire" to "4 min read - releases",
                "How the picker holds state" to "Variant survives detail navigation.",
                "Item three: navigation rules" to "NamedAction(navigate) is the only push.",
                "Saving for later, on the server" to "HostAction(1002) explained.",
                "Round trip times this week" to "30 ms median, 91 ms p99.",
            )
            titles.forEachIndexed { i, (title, note) ->
                val n = i + 1
                catalogRow(title, note, Actions.navigateItem(n))
            }
            textCta("Refresh", Actions.REFRESH, align = BOX_CENTER)
        }
    }
}

fun itemDocument(id: Int, viewport: Viewport): ByteArray = render("Brief - Item $id", viewport) {
    root {
        column(modifier = pageColumn()) {
            eyebrow("Releases")
            text(
                string = "Item $id, notes from a quiet wire",
                color = INK_PRIMARY,
                fontSize = 30f,
                fontStyle = ITALIC,
                fontWeight = 600f,
                fontFamily = FAM_DISPLAY,
            )
            text(
                string = "BY THE WIRE - 4 MIN READ",
                color = INK_TERTIARY,
                fontSize = 11f,
                fontWeight = 500f,
                fontFamily = FAM_BODY,
                letterSpacing = 0.08f,
            )
            bodyParagraph(
                "The server emits a binary Compose Remote document. The client is a thin renderer " +
                    "that fetches the bytes, draws them, and routes user actions back through the server.",
            )
            bodyParagraph(
                "No layout decisions live in the app. Three home variants share one route, and the " +
                    "detail page is reached only by tapping a Catalog row.",
            )
            bodyParagraph(
                "Navigation is server authored too. The client treats NamedAction(navigate, url) as " +
                    "the only way to open a new screen.",
            )
            primaryCta("Mark read", Actions.MARK_READ)
            textCta("Open in source", Actions.OPEN_IN_SOURCE)
        }
    }
}

// ---- Catalog row + spacer -------------------------------------------------

private fun RemoteComposeContext.catalogRow(title: String, note: String, actionId: Int) {
    // Accent top divider.
    box(
        modifier = RecordingModifier()
            .fillMaxWidth()
            .height(1f)
            .background(ACCENT),
    ) {}
    // Row: title/note on the left, chevron on the right. Whole strip is clickable.
    row(
        modifier = RecordingModifier()
            .fillMaxWidth()
            .padding(0, 14, 0, 14)
            .onClick(HostAction(actionId)),
        horizontal = BOX_START,
        vertical = BOX_CENTER,
    ) {
        column(
            modifier = RecordingModifier().horizontalWeight(1f).spacedBy(2f),
        ) {
            text(
                string = title,
                color = INK_PRIMARY,
                fontSize = 18f,
                fontWeight = 500f,
                fontFamily = FAM_BODY,
            )
            text(
                string = note,
                color = INK_SECONDARY,
                fontSize = 13f,
                fontFamily = FAM_BODY,
            )
        }
        text(
            string = "›",
            color = INK_TERTIARY,
            fontSize = 22f,
            fontWeight = 400f,
            fontFamily = FAM_BODY,
        )
    }
}


