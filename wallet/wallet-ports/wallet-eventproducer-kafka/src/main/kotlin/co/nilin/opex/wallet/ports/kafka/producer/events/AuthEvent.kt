package co.nilin.opex.wallet.ports.kafka.producer.events

import java.time.LocalDateTime

open class AuthEvent {
    var eventDate: LocalDateTime = LocalDateTime.now()
}