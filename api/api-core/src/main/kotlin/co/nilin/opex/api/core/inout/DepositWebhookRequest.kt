package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class DepositWebhookRequest(
    val referenceNumber: String,
    val depositNumber: String,
    val symbol: String,
    val amount: BigDecimal,
    val externalIdentifier: String,
    val date: Long
)
