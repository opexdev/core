package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.model.WalletType
import java.math.BigDecimal

interface WalletProxy {

    suspend fun transfer(
        symbol: String,
        senderWalletType: WalletType,
        senderUuid: String,
        receiverWalletType: WalletType,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        transferCategory: String
    )

    suspend fun canFulfil(symbol: String, walletType: WalletType, uuid: String, amount: BigDecimal): Boolean
}