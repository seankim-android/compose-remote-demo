package dev.seankim.composeremote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.*

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        configure()
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `default variant returns brief bytes`() = testApplication {
        configure()
        val res = client.get("/screens/home")
        assertEquals(HttpStatusCode.OK, res.status)
        assertTrue(res.bodyAsBytes().isNotEmpty())
    }

    @Test
    fun `each variant returns distinct payloads`() = testApplication {
        configure()
        val brief = client.get("/screens/home?variant=brief").bodyAsBytes()
        val sparse = client.get("/screens/home?variant=sparse").bodyAsBytes()
        val catalog = client.get("/screens/home?variant=catalog").bodyAsBytes()
        assertTrue(brief.isNotEmpty() && sparse.isNotEmpty() && catalog.isNotEmpty())
        assertFalse(brief.contentEquals(sparse), "brief and sparse should differ")
        assertFalse(brief.contentEquals(catalog), "brief and catalog should differ")
        assertFalse(sparse.contentEquals(catalog), "sparse and catalog should differ")
    }

    @Test
    fun `item detail returns bytes for the requested id`() = testApplication {
        configure()
        val one = client.get("/screens/item?id=1").bodyAsBytes()
        val three = client.get("/screens/item?id=3").bodyAsBytes()
        assertTrue(one.isNotEmpty() && three.isNotEmpty())
        assertFalse(one.contentEquals(three), "different ids should differ in payload")
    }

    @Test
    fun `unknown variant falls back to brief`() = testApplication {
        configure()
        val unknown = client.get("/screens/home?variant=nope").bodyAsBytes()
        val brief = client.get("/screens/home?variant=brief").bodyAsBytes()
        assertContentEquals(brief, unknown)
    }
}
