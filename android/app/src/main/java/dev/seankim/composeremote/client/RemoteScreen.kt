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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.seankim.composeremote.client.ui.theme.DisplayFont
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewport = rememberViewport()
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
            result = fetch(current.url(viewport))
        } catch (e: Exception) {
            error = e.message ?: e::class.java.simpleName
        }
    }

    LaunchedEffect(current, viewport) { load() }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    // The server emits HostAction(id) on tappable elements; this dispatches by id
    // into local UI work. Action ids are defined in RemoteActions.kt and mirrored
    // in the server's compositions/Variants.kt.
    fun onAction(id: Int) {
        when (id) {
            in RemoteActions.ITEM_RANGE -> backStack.add(Screen.Item(RemoteActions.itemIdFor(id)))
            RemoteActions.READ_FEATURED -> scope.launch { snackbar.showSnackbar("Opened featured release") }
            RemoteActions.SAVE_FOR_LATER -> {
                savedForLater = !savedForLater
                scope.launch {
                    snackbar.showSnackbar(if (savedForLater) "Saved for later" else "Removed from saved")
                }
            }
            RemoteActions.CATCH_ME_UP -> {
                backStack[backStack.lastIndex] = Screen.Home(Variant.CATALOG)
            }
            RemoteActions.REFRESH -> scope.launch {
                refreshing = true
                load()
                refreshing = false
            }
            RemoteActions.MARK_READ -> {
                pop()
                scope.launch { snackbar.showSnackbar("Marked read") }
            }
            RemoteActions.OPEN_IN_SOURCE -> {
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
                            Text(
                                "Could not reach ${current.url(viewport)}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(error!!, style = MaterialTheme.typography.bodyLarge)
                        }
                        result == null -> Text("Loading...")
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
