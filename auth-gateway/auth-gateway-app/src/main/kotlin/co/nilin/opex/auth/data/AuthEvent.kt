package co.nilin.opex.auth.data

import java.time.LocalDateTime

open class AuthEvent {

    val time: LocalDateTime = LocalDateTime.now()
}