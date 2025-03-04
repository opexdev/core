package co.nilin.opex.matching.gateway.ports.postgres.dto

import java.time.LocalDateTime

class PairSetting(
    val pair: String,
    val isAvailable: Boolean = true,
    val updateDate: LocalDateTime? = null,
)