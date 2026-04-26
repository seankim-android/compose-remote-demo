package dev.seankim.composeremote.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private const val SERVER_URL = "http://10.0.2.2:8080/screens/home"

private data class FetchResult(
    val bytes: ByteArray,
    val latencyMs: Long,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(modifier: Modifier = Modifier) {
    var result by remember { mutableStateOf<FetchResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var inspectorOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            result = withContext(Dispatchers.IO) {
                val start = System.currentTimeMillis()
                val data = URL(SERVER_URL).openStream().use { it.readBytes() }
                FetchResult(bytes = data, latencyMs = System.currentTimeMillis() - start)
            }
        } catch (e: Exception) {
            error = e.message ?: e::class.java.simpleName
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Brief") },
                actions = {
                    IconButton(
                        onClick = { inspectorOpen = true },
                        enabled = result != null,
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = "Inspect wire payload")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                error != null -> Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Could not reach $SERVER_URL", style = MaterialTheme.typography.titleMedium)
                    Text(error!!, style = MaterialTheme.typography.bodyLarge)
                }
                result == null -> Text("Loading…")
                else -> RemotePlayer(bytes = result!!.bytes, modifier = Modifier.fillMaxSize())
            }
        }
    }

    if (inspectorOpen && result != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { inspectorOpen = false },
            sheetState = sheetState,
        ) {
            InspectorContent(url = SERVER_URL, result = result!!)
        }
    }
}

@Composable
private fun InspectorContent(url: String, result: FetchResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Wire payload", style = MaterialTheme.typography.titleMedium)
        Field("URL", url)
        Field("Size", "${result.bytes.size} bytes")
        Field("Round trip", "${result.latencyMs} ms")
        Text("First 256 bytes (hex)", style = MaterialTheme.typography.labelLarge)
        Text(
            text = hexPreview(result.bytes, max = 256),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        )
    }
}

@Composable
private fun Field(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun hexPreview(bytes: ByteArray, max: Int): String {
    val take = bytes.size.coerceAtMost(max)
    val builder = StringBuilder(take * 3)
    for (i in 0 until take) {
        if (i > 0 && i % 16 == 0) builder.append('\n')
        else if (i > 0) builder.append(' ')
        val b = bytes[i].toInt() and 0xFF
        builder.append(HEX[b ushr 4]).append(HEX[b and 0x0F])
    }
    if (bytes.size > take) builder.append("\n…")
    return builder.toString()
}

private val HEX = "0123456789abcdef".toCharArray()

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
