package co.nilin.opex.auth.data

import java.time.LocalDateTime

data class Sessions(
    val deviceUuid: String?,
    val os: Os?,
    val osVersion: String?,
    val appVersion: String?,
    val firstLoginDate: LocalDateTime?,
    val lastLoginDate: LocalDateTime?,
    val sessionState: String?,
    val sessionStatus: SessionStatus?,
    val sessionCreateDate: LocalDateTime?,
    val sessionExpireDate: LocalDateTime?,
    var isCurrentSession: Boolean?=false
)

enum class SessionStatus {
    ACTIVE,
    EXPIRED,
    TERMINATED
}

enum class Os {
    ANDROID, IOS, MOBILE_WEB, DESKTOP_WEB
}
