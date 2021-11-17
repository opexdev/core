package co.nilin.opex.port.websocket.service.stream

import java.security.Principal
import java.util.*

data class Subscription(
    val sessionId: String,
    val user: Principal? = null,
    val time: Long = Date().time
)