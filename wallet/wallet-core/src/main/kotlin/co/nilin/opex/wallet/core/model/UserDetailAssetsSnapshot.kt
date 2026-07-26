package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class UserDetailAssetsSnapshotRaw(
    val uuid: String,
    val currencySnapshots: String,
    val totalAmount: BigDecimal,
    val quoteCurrency: String,
    val snapshotDate: LocalDateTime
)

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