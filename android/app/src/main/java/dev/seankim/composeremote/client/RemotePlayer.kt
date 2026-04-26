// The only file in this app that imports androidx.compose.remote.*. Keep all
// AndroidX Compose Remote usage here so the rest of the app does not depend on
// the alpha API surface.
package dev.seankim.composeremote.client

import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import androidx.compose.remote.player.core.RemoteDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun RemotePlayer(
    bytes: ByteArray,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val document = remember(bytes) { RemoteDocument(bytes) }
    RemoteDocumentPlayer(
        document = document.document,
        documentWidth = document.width,
        documentHeight = document.height,
        modifier = modifier,
        onAction = { id, _ -> onAction(id) },
    )
}
