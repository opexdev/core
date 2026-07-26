package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class UserDetailAssetsSnapshot(
    val uuid: String,
    val currencySnapshots: List<CurrencyAssetsSnapshot>,
    val totalAmount: BigDecimal,
    val quoteCurrency: String,
    val snapshotDate: LocalDateTime,
)

data class CurrencyAssetsSnapshot(
    val currency: String,
    val volume: BigDecimal,
)