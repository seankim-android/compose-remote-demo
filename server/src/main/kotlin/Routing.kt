package dev.seankim.composeremote

import dev.seankim.composeremote.compositions.Variant
import dev.seankim.composeremote.compositions.documentFor
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("compose-remote-demo. See /screens/home (variants: brief, sparse, catalog).")
        }
        get("/screens/home") {
            val variant = Variant.parse(call.request.queryParameters["variant"])
            call.respondBytes(documentFor(variant), ContentType.Application.OctetStream)
        }
    }
}
