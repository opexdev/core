package co.nilin.opex.auth.data

data class LogoutEvent(
    val uuid: String,
    val sessionId: String?,
    val logOutOthers: Boolean? = false
) : AuthEvent()