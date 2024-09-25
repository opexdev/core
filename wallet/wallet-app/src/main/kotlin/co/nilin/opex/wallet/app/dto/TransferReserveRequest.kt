package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.WalletType
import java.math.BigDecimal

data class TransferReserveRequest(
    val sourceAmount: BigDecimal,
    val sourceSymbol: String,
    val destSymbol: String,
    var senderUuid: String?,
    val senderWalletType: WalletType,
    val receiverUuid: String,
    val receiverWalletType: WalletType
)