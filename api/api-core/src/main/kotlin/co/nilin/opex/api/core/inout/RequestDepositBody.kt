package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class RequestDepositBody(
    val symbol: String,
    val receiverUuid: String,
    val receiverWalletType: WalletType,
    val amount: BigDecimal,
    val description: String?,
    val transferRef: String?,
    val gatewayUuid: String?,
    val chain: String?,
)
