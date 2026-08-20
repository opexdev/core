package co.nilin.opex.auth.data

import java.time.LocalDateTime

data class LoginEvent(
    val uuid: String,
    val deviceUuid: String?,
    val appVersion: String?,
    val osVersion: String?,
    val pushToken: String?,
    val os: Os?,
    val brand: String?,
    val model: String?,
    val platform: Platform?,
    val agent: String?,
    val buildNumber: Int?,
    val sessionId: String,
    val expireDate: LocalDateTime
) : AuthEvent()