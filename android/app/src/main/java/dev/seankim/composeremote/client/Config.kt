package dev.seankim.composeremote.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity

// 10.0.2.2 is the Android emulator's loopback to the host machine.
const val BASE_URL: String = "http://10.0.2.2:8080"

// w/h are dp; density is px-per-dp. The server multiplies authored dp values
// by density before writing them to the doc, so fontSize=14 ends up rendering
// at 14sp instead of 14 raw px (~5sp on a 2.625x device).
data class Viewport(val w: Int, val h: Int, val density: Float)

@Composable
fun rememberViewport(): Viewport {
    val config = LocalConfiguration.current
    val density = LocalDensity.current.density
    val widthDp = config.screenWidthDp.coerceAtLeast(280)
    // Subtract top app bar (64dp), variant picker (~56dp), and a buffer for
    // system insets so we approximate the area the player actually occupies.
    val contentHeightDp = (config.screenHeightDp - 140).coerceAtLeast(400)
    return remember(widthDp, contentHeightDp, density) {
        Viewport(w = widthDp, h = contentHeightDp, density = density)
    }
}
