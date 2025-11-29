package co.nilin.opex.device.core.data

import java.time.LocalDateTime

data class Session(
    val sessionState: String,
    val userId: String,
    val deviceId: Long,
    val status: SessionStatus= SessionStatus.ACTIVE,
    val createDate: LocalDateTime?= LocalDateTime.now(),
    val expireDate: LocalDateTime?
)
