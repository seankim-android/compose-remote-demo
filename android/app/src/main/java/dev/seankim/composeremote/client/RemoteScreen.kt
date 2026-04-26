package dev.seankim.composeremote.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import androidx.compose.remote.player.core.RemoteDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private const val SERVER_URL = "http://10.0.2.2:8080/screens/home"

@Composable
fun RemoteScreen(modifier: Modifier = Modifier) {
    var bytes by remember { mutableStateOf<ByteArray?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            bytes = withContext(Dispatchers.IO) {
                URL(SERVER_URL).openStream().use { it.readBytes() }
            }
        } catch (e: Exception) {
            error = e.message ?: e::class.java.simpleName
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            error != null -> Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Could not reach $SERVER_URL", style = MaterialTheme.typography.titleMedium)
                Text(error!!, style = MaterialTheme.typography.bodyLarge)
            }
            bytes == null -> Text("Loading…")
            else -> RemotePlayer(bytes = bytes!!, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun RemotePlayer(bytes: ByteArray, modifier: Modifier = Modifier) {
    val document = remember(bytes) { RemoteDocument(bytes) }
    RemoteDocumentPlayer(
        document = document.document,
        documentWidth = document.width,
        documentHeight = document.height,
        modifier = modifier,
    )
}
