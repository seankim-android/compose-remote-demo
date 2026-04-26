package dev.seankim.composeremote

import dev.seankim.composeremote.compositions.Variant
import dev.seankim.composeremote.compositions.Viewport
import dev.seankim.composeremote.compositions.descriptorFor
import dev.seankim.composeremote.compositions.documentFor
import dev.seankim.composeremote.compositions.itemDescriptor
import dev.seankim.composeremote.compositions.itemDocument
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText(
                "compose-remote-demo. Routes: /screens/home?variant=brief|sparse|catalog, /screens/item?id=N",
            )
        }
        get("/screens/home") {
            val q = call.request.queryParameters
            val variant = Variant.parse(q["variant"])
            val viewport = Viewport.parse(q["w"], q["h"], q["density"])
            if (q["format"] == "json") {
                call.respond(descriptorFor(variant))
            } else {
                call.respondBytes(documentFor(variant, viewport), ContentType.Application.OctetStream)
            }
        }
        get("/screens/item") {
            val q = call.request.queryParameters
            val id = q["id"]?.toIntOrNull() ?: 1
            val viewport = Viewport.parse(q["w"], q["h"], q["density"])
            if (q["format"] == "json") {
                call.respond(itemDescriptor(id))
            } else {
                call.respondBytes(itemDocument(id, viewport), ContentType.Application.OctetStream)
            }
        }
    }
}
