package co.nilin.opex.device.core.data

import java.time.LocalDateTime

data class UserSessionDevice(
    val deviceUuid: String?,
    val os: Os?,
    val osVersion: String?,
    val appVersion: String?,
    val firstLoginDate: LocalDateTime?,
    val lastLoginDate: LocalDateTime?,
    val ipAddress: String?,
    val sessionState: String?,
    val sessionStatus: SessionStatus?,
    val sessionCreateDate: LocalDateTime?,
    val sessionExpireDate: LocalDateTime?
)