package dev.seankim.composeremote

import dev.seankim.composeremote.compositions.briefDocument
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("compose-remote-demo. See /screens/home.")
        }
        get("/screens/home") {
            call.respondBytes(briefDocument(), ContentType.Application.OctetStream)
        }
    }
}
