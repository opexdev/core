package co.nilin.opex.matching.gateway.ports.postgres.dto

import co.nilin.opex.matching.gateway.ports.postgres.model.PairCategory
import java.math.BigDecimal
import java.time.LocalDateTime

class PairSetting(
    val pair: String,
    val isAvailable: Boolean,
    val minOrder: BigDecimal,
    val maxOrder: BigDecimal,
    val orderTypes: String,
    val updateDate: LocalDateTime? = null,
    val internalChart: Boolean,
    val globalChart: Boolean,
    val categories: List<PairCategory> = emptyList()
)