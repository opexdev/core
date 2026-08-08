package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

class PairSetting(
    val pair: String,
    val isAvailable: Boolean,
    val minOrder : BigDecimal,
    val maxOrder : BigDecimal,
    val orderTypes : String,
    val updateDate: LocalDateTime? = null,
    val internalChart: Boolean,
    val globalChart: Boolean,
    val categories: List<PairCategory> = emptyList()
)