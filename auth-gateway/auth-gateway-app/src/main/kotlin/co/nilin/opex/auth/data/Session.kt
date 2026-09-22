package co.nilin.opex.auth.data

import java.time.LocalDateTime

data class Sessions(
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
    val sessionExpireDate: LocalDateTime?,
    var isCurrentSession: Boolean? = false
)

enum class SessionStatus {
    ACTIVE,
    EXPIRED,
    TERMINATED
}

enum class Os {
    ANDROID, IOS,IPADOS, WINDOWS, MACOS,LINUX,CHROMEOS,OTHER,UNKNOWN
}

enum class Platform {
    ANDROID_APP, IOS_APP, MOBILE_WEB, DESKTOP_WEB, TABLET_WEB,ADMIN_WEB
}
