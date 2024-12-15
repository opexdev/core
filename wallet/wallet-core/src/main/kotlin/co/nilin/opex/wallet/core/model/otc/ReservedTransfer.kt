package co.nilin.opex.wallet.core.model.otc

import co.nilin.opex.wallet.core.model.WalletType
import java.math.BigDecimal
import java.time.LocalDateTime


data class ReservedTransfer(
    var id: Long? = null,
    var reserveNumber: String,
    var sourceSymbol: String,
    var destSymbol: String,
    var senderWalletType: WalletType,
    var senderUuid: String,
    var receiverWalletType: WalletType,
    var receiverUuid: String,
    var sourceAmount: BigDecimal,
    var reservedDestAmount: BigDecimal,
    var reserveDate: LocalDateTime? = LocalDateTime.now(),
    var expDate: LocalDateTime? = null,
    var status: ReservedStatus? = null,
    val rate: BigDecimal? = null,
)


enum class ReservedStatus {
    Created, Expired, Committed,
}
