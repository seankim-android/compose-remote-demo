package dev.seankim.composeremote.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun InspectorContent(result: FetchResult) {
    var jsonText by remember { mutableStateOf<String?>(null) }
    var jsonError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(result.url) {
        jsonText = null
        jsonError = null
        try {
            val jsonUrl = if (result.url.contains('?')) "${result.url}&format=json"
            else "${result.url}?format=json"
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
        Text(
            "Wire payload (JSON)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        when {
            jsonError != null -> Text(
                text = jsonError!!,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            jsonText != null -> Text(
                text = jsonText!!,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            else -> Text("Loading...", style = MaterialTheme.typography.bodySmall)
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
