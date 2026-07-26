package co.nilin.opex.device.core.data

import java.time.LocalDateTime


data class LoginEvent(
    val uuid: String,
    val deviceUuid: String?,
    val appVersion: String?,
    val osVersion: String?,
    val pushToken: String?,
    val os: Os?,
    val sessionId: String,
    val expireDate: LocalDateTime,
    ) : SessionEvent()

