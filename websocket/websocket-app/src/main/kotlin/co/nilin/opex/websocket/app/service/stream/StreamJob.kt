package co.nilin.opex.websocket.app.service.stream

import java.util.concurrent.TimeUnit

data class StreamJob(
    val interval: Long,
    val timeUnit: TimeUnit,
    val runnable: suspend () -> Any
)