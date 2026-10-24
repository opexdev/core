package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class DepositWebhookRequest(
    val referenceNumber: String,
    val destDepositNumber: String,
    val sourceDepositNumber: String? = null,
    val symbol: String,
    val amount: BigDecimal,
    val externalIdentifier: String? = null,
    val date: Long
)
