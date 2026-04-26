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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import androidx.compose.remote.player.core.RemoteDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

private const val BASE_URL = "http://10.0.2.2:8080/screens/home"

private enum class Variant(val slug: String, val label: String) {
    BRIEF("brief", "Brief"),
    SPARSE("sparse", "Sparse"),
    CATALOG("catalog", "Catalog"),
}

private fun variantUrl(variant: Variant) = "$BASE_URL?variant=${variant.slug}"

private data class FetchResult(
    val url: String,
    val bytes: ByteArray,
    val latencyMs: Long,
)

private suspend fun fetch(variant: Variant): FetchResult = withContext(Dispatchers.IO) {
    val url = variantUrl(variant)
    val start = System.currentTimeMillis()
    val data = URL(url).openStream().use { it.readBytes() }
    FetchResult(url = url, bytes = data, latencyMs = System.currentTimeMillis() - start)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(modifier: Modifier = Modifier) {
    var variant by remember { mutableStateOf(Variant.BRIEF) }
    var result by remember { mutableStateOf<FetchResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var inspectorOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        try {
            error = null
            result = fetch(variant)
        } catch (e: Exception) {
            error = e.message ?: e::class.java.simpleName
        }
    }

    LaunchedEffect(variant) { load() }

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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            VariantPicker(
                selected = variant,
                onSelect = { variant = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = {
                    refreshing = true
                    scope.launch {
                        load()
                        refreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when {
                        error != null -> Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Could not reach ${variantUrl(variant)}", style = MaterialTheme.typography.titleMedium)
                            Text(error!!, style = MaterialTheme.typography.bodyLarge)
                        }
                        result == null -> Text("Loading…")
                        else -> RemotePlayer(bytes = result!!.bytes, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    if (inspectorOpen && result != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { inspectorOpen = false },
            sheetState = sheetState,
        ) {
            InspectorContent(result = result!!)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VariantPicker(
    selected: Variant,
    onSelect: (Variant) -> Unit,
    modifier: Modifier = Modifier,
) {
    val variants = Variant.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        variants.forEachIndexed { index, v ->
            SegmentedButton(
                selected = v == selected,
                onClick = { onSelect(v) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = variants.size),
            ) {
                Text(v.label)
            }
        }
    }
}

@Composable
private fun InspectorContent(result: FetchResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Wire payload", style = MaterialTheme.typography.titleMedium)
        Field("URL", result.url)
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
