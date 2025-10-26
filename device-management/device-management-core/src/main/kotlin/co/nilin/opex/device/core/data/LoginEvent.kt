package co.nilin.opex.device.core.data

import java.time.LocalDateTime

data class LoginEvent(
    val userId: Long,
    val sessionId: Long,
    val deviceUuid: String,
    val appVersion: String,
    val osVersion: String,
    val os: String,
    val eventDte: LocalDateTime
)
