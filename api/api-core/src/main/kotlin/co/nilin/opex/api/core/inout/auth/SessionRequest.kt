package co.nilin.opex.api.core.inout.auth

data class SessionRequest(
    var uuid: String? = null,
    val limit: Int = 10,
    val offset: Int = 0,
    val ascendingByTime: Boolean = false,
    val os: Os? = null,
    val status: SessionStatus? = null
)