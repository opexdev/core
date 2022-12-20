package co.nilin.opex.accountant.core.inout

import java.math.BigDecimal

data class TransferRequest(
    val amount: BigDecimal,
    val symbol: String,
    val senderUuid: String,
    val senderWalletType: String,
    val receiverUuid: String,
    val receiverWalletType: String,
    val transferRef: String?,
    val description: String?
)