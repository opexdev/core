package co.nilin.opex.wallet.ports.postgres.dto

import java.math.BigDecimal


data class OffChainGatewayView(
    var id: Long?,
    val gatewayUuid: String,
    val currencySymbol: String,
    var withdrawAllowed: Boolean? = true,
    var depositAllowed: Boolean? = true,
    var withdrawFee: BigDecimal? = BigDecimal.ZERO,
    var withdrawMin: BigDecimal? = BigDecimal.ZERO,
    var withdrawMax: BigDecimal? = BigDecimal.ZERO,
    var depositMin: BigDecimal? = BigDecimal.ZERO,
    var depositMax: BigDecimal? = BigDecimal.ZERO,
    var transferMethod: String,
    var isDepositActive: Boolean? = true,
    var isWithdrawActive: Boolean? = true,
    val depositDescription: String?,
    val withdrawDescription: String?,
    val displayOrder: Int? = null,

    )