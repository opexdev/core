package co.nilin.opex.auth.gateway.model

import java.time.LocalDateTime

open class AuthEvent {
    var eventDate: LocalDateTime = LocalDateTime.now()
}