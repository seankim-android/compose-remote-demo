package dev.seankim.composeremote.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

// 10.0.2.2 is the Android emulator's loopback to the host machine.
const val BASE_URL: String = "http://10.0.2.2:8080"

// The viewport sent to the server. With SIZING_LAYOUT enabled on the doc, the
// player lays content out to fill its actual size, so these values are
// effectively a hint to the server about the room it has to author for.
data class Viewport(val w: Int, val h: Int)

@Composable
fun rememberViewport(): Viewport {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp.coerceAtLeast(280)
    // Subtract top app bar (64dp), variant picker (~56dp), and a buffer for
    // system insets so we approximate the area the player actually occupies.
    val contentHeightDp = (config.screenHeightDp - 140).coerceAtLeast(400)
    return remember(widthDp, contentHeightDp) {
        Viewport(w = widthDp, h = contentHeightDp)
    }
}
