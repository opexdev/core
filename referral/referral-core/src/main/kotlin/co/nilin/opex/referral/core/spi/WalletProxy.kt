package co.nilin.opex.referral.core.spi

import java.math.BigDecimal

interface WalletProxy {
    suspend fun transfer(
        symbol: String,
        senderWalletType: String,
        senderUuid: String,
        receiverWalletType: String,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
    )

    suspend fun canFulfil(symbol: String, walletType: String, uuid: String, amount: BigDecimal): Boolean
}