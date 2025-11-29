package co.nilin.opex.device.core.data


data class LoginEvent(
    val uuid: String,
    val deviceUuid: String?,
    val appVersion: String?,
    val osVersion: String?,
    val pushToken: String?,
    val os: String?,
    val sessionId: String
): SessionEvent()

