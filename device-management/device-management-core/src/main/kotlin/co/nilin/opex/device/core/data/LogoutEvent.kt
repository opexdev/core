package co.nilin.opex.device.core.data


data class LogoutEvent(
    val uuid: String,
    val logoutOthers: Boolean?=false,
    val sessionId: String
) : SessionEvent()