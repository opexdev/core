package co.nilin.opex.device.core.data

data class SessionsRequest(
    val uuid: String,
    val limit: Int = 10,
    val offset: Int = 0,
    val ascendingByTime: Boolean = false,
    val os: Os?=null,
    val status: SessionStatus? = null
)