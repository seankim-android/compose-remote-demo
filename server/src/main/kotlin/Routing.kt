package dev.seankim.composeremote

import dev.seankim.composeremote.compositions.Variant
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
            val variant = Variant.parse(call.request.queryParameters["variant"])
            if (call.request.queryParameters["format"] == "json") {
                call.respond(descriptorFor(variant))
            } else {
                call.respondBytes(documentFor(variant), ContentType.Application.OctetStream)
            }
        }
        get("/screens/item") {
            val id = call.request.queryParameters["id"]?.toIntOrNull() ?: 1
            if (call.request.queryParameters["format"] == "json") {
                call.respond(itemDescriptor(id))
            } else {
                call.respondBytes(itemDocument(id), ContentType.Application.OctetStream)
            }
        }
    }
}
