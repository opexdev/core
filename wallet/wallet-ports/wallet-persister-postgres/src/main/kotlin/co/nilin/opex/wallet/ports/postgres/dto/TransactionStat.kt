package co.nilin.opex.wallet.ports.postgres.dto

import java.math.BigDecimal

class TransactionStat(
    val cnt: Long?,
    val total: BigDecimal?
)