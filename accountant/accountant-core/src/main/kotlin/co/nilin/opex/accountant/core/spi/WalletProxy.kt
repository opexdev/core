package co.nilin.opex.accountant.core.spi

import co.nilin.opex.accountant.core.inout.TransferRequest
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
        transferRef: String?
    )

    suspend fun batchTransfer(transfers: List<TransferRequest>)

    suspend fun canFulfil(symbol: String, walletType: String, uuid: String, amount: BigDecimal): Boolean
}