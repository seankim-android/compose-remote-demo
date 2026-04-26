package dev.seankim.composeremote.client

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import androidx.compose.remote.player.core.RemoteDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.seankim.composeremote.client.ui.theme.DisplayFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

private const val BASE = "http://10.0.2.2:8080"

private enum class Variant(val slug: String, val label: String) {
    BRIEF("brief", "Brief"),
    SPARSE("sparse", "Sparse"),
    CATALOG("catalog", "Catalog"),
}

private sealed interface Screen {
    val title: String
    val url: String

    data class Home(val variant: Variant) : Screen {
        override val title = "Brief"
        override val url = "$BASE/screens/home?variant=${variant.slug}"
    }

    data class Item(val id: Int) : Screen {
        override val title = "Item $id"
        override val url = "$BASE/screens/item?id=$id"
    }
}

private data class FetchResult(
    val url: String,
    val bytes: ByteArray,
    val latencyMs: Long,
)

private suspend fun fetch(url: String): FetchResult = withContext(Dispatchers.IO) {
    val start = System.currentTimeMillis()
    val data = URL(url).openStream().use { it.readBytes() }
    FetchResult(url = url, bytes = data, latencyMs = System.currentTimeMillis() - start)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home(Variant.BRIEF)) }
    val current = backStack.last()
    var result by remember { mutableStateOf<FetchResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var inspectorOpen by remember { mutableStateOf(false) }
    var savedForLater by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        try {
            error = null
            result = fetch(current.url)
        } catch (e: Exception) {
            error = e.message ?: e::class.java.simpleName
        }
    }

    LaunchedEffect(current) { load() }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun onAction(id: Int) {
        when (id) {
            in 2001..2099 -> backStack.add(Screen.Item(id - 2000))
            1001 -> scope.launch { snackbar.showSnackbar("Opened featured release") }
            1002 -> {
                savedForLater = !savedForLater
                scope.launch {
                    snackbar.showSnackbar(if (savedForLater) "Saved for later" else "Removed from saved")
                }
            }
            1003 -> {
                backStack[backStack.lastIndex] = Screen.Home(Variant.CATALOG)
            }
            1004 -> scope.launch {
                refreshing = true
                load()
                refreshing = false
            }
            3001 -> {
                pop()
                scope.launch { snackbar.showSnackbar("Marked read") }
            }
            3002 -> {
                val itemId = (current as? Screen.Item)?.id ?: 0
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/item/$itemId"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
                Unit
            }
            else -> scope.launch { snackbar.showSnackbar("Action $id") }
        }
    }

    BackHandler(enabled = backStack.size > 1) { pop() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        current.title,
                        fontStyle = FontStyle.Italic,
                        fontFamily = DisplayFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                navigationIcon = {
                    if (backStack.size > 1) {
                        IconButton(onClick = { pop() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
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
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            (current as? Screen.Home)?.let { home ->
                VariantPicker(
                    selected = home.variant,
                    onSelect = { backStack[backStack.lastIndex] = Screen.Home(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
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
                            Text("Could not reach ${current.url}", style = MaterialTheme.typography.titleMedium)
                            Text(error!!, style = MaterialTheme.typography.bodyLarge)
                        }
                        result == null -> Text("Loading…")
                        else -> RemotePlayer(
                            bytes = result!!.bytes,
                            onAction = ::onAction,
                            modifier = Modifier.fillMaxSize(),
                        )
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
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    activeBorderColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(v.label)
            }
        }
    }
}

@Composable
private fun InspectorContent(result: FetchResult) {
    var jsonText by remember { mutableStateOf<String?>(null) }
    var jsonError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(result.url) {
        jsonText = null
        jsonError = null
        try {
            val jsonUrl = if (result.url.contains('?')) "${result.url}&format=json" else "${result.url}?format=json"
            jsonText = withContext(Dispatchers.IO) { URL(jsonUrl).readText() }
        } catch (e: Exception) {
            jsonError = e.message ?: e::class.java.simpleName
        }
    }

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
        Text("Wire payload (JSON)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        when {
            jsonError != null -> Text(
                text = jsonError!!,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            jsonText != null -> Text(
                text = jsonText!!,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            else -> Text("Loading…", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Field(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RemotePlayer(
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
