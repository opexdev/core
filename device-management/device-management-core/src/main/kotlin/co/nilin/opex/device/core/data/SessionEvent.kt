package co.nilin.opex.device.core.data

import java.time.LocalDateTime

open class SessionEvent {
    val time: LocalDateTime = LocalDateTime.now()
}

