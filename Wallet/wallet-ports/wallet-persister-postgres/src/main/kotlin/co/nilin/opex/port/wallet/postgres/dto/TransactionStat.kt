package co.nilin.opex.port.wallet.postgres.dto

import java.math.BigDecimal

class TransactionStat(
    val cnt: Long?,
    val total: BigDecimal?
)