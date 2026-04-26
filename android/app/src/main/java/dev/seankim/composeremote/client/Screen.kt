package dev.seankim.composeremote.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

enum class Variant(val slug: String, val label: String) {
    BRIEF("brief", "Brief"),
    SPARSE("sparse", "Sparse"),
    CATALOG("catalog", "Catalog"),
}

sealed interface Screen {
    val title: String
    fun url(viewport: Viewport): String

    data class Home(val variant: Variant) : Screen {
        override val title = "Brief"
        override fun url(viewport: Viewport) =
            "$BASE_URL/screens/home?variant=${variant.slug}&w=${viewport.w}&h=${viewport.h}&density=${viewport.density}"
    }

    data class Item(val id: Int) : Screen {
        override val title = "Item $id"
        override fun url(viewport: Viewport) =
            "$BASE_URL/screens/item?id=$id&w=${viewport.w}&h=${viewport.h}&density=${viewport.density}"
    }
}

data class FetchResult(
    val url: String,
    val bytes: ByteArray,
    val latencyMs: Long,
)

suspend fun fetch(url: String): FetchResult = withContext(Dispatchers.IO) {
    val start = System.currentTimeMillis()
    val data = URL(url).openStream().use { it.readBytes() }
    FetchResult(url = url, bytes = data, latencyMs = System.currentTimeMillis() - start)
}
