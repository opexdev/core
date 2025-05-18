package co.nilin.opex.matching.gateway.ports.postgres.dto

import java.math.BigDecimal
import java.time.LocalDateTime

class PairSetting(
    val pair: String,
    val isAvailable: Boolean,
    val minOrder : BigDecimal,
    val maxOrder : BigDecimal,
    val orderTypes : String,
    val updateDate: LocalDateTime? = null,
)