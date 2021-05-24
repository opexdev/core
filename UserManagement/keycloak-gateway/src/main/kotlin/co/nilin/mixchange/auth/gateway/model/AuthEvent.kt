package co.nilin.mixchange.auth.gateway.model

import java.time.LocalDateTime

open class AuthEvent {
    var eventDate: LocalDateTime = LocalDateTime.now()
}