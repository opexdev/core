package co.nilin.opex.auth.data

data class LogoutEvent(
    val uuid: String,
    val sessionId: String?,
    val logoutOthers: Boolean? = false
) : AuthEvent()