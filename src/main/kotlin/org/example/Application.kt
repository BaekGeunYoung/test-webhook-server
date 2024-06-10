package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.toMap
import io.portone.webhook.Webhook

fun main(vararg args: String) {
    val secret = args.getOrNull(0)

    if (secret == null) println("No secret provided")
    else if (!secret.startsWith("whsec_")) println("Secret must start with whsec_")
    else {
        println("Provided secret is [$secret]")
        println("Starting Server...\n\n")
        embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { module(secret) }).start(wait = true)
    }
}

fun Application.module(secret: String) {
    val verifier = Webhook(secret)
    configureRouting(verifier)
}

fun Application.configureRouting(webhookVerifier: Webhook) {
    routing {
        post("/webhook-test") {
            val payload = call.receiveText()
            val header = java.net.http.HttpHeaders.of(call.request.headers.toMap()) { _, _ -> true }

            println("Webhook received")
            println("Payload: $payload")
            println("Header: $header")

            try {
                webhookVerifier.verify(payload, header)
                println("Webhook Verification Succeeded")

                call.respond(HttpStatusCode.OK, "OK")
            } catch (e: Exception) {
                val msg = "Webhook Verification Failed: $e"
                println(msg)

                call.respond(HttpStatusCode.BadRequest, msg)
            }
        }

        post("/confirm-test") {
            val payload = call.receiveText()
            val header = java.net.http.HttpHeaders.of(call.request.headers.toMap()) { _, _ -> true }

            println("Webhook received")
            println("Payload: $payload")
            println("Header: $header")

            try {
                webhookVerifier.verify(payload, header)
                println("Webhook Verification Succeeded")

                call.respond(HttpStatusCode.OK, "{\"errorMessage\":\"컨펌 에러 메세지 테스트\"}")
            } catch (e: Exception) {
                val msg = "Webhook Verification Failed: $e"
                println(msg)

                call.respond(HttpStatusCode.BadRequest, msg)
            }
        }
    }
}
