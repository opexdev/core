package co.nilin.opex.wallet.ports.kafka.listener.model

import java.time.LocalDateTime

open class AuthEvent {
    var eventDate: LocalDateTime = LocalDateTime.now()
}