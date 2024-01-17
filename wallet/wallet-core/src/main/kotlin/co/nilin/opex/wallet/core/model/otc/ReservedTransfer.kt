package co.nilin.opex.wallet.core.model.otc

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class ReservedTransfer(var id: Long? = null,
                            var reserveNumber: String,
                            var sourceSymbol: String,
                            var destSymbol: String,
                            var senderWalletType: String,
                            var senderUuid: String,
                            var receiverWalletType: String,
                            var receiverUuid: String,
                            var sourceAmount: BigDecimal,
                            var reservedDestAmount: BigDecimal,
                            var reserveDate: LocalDateTime? = LocalDateTime.now(),
                            var expDate: LocalDateTime?=null,
                            var status: ReservedStatus?=null)


enum class ReservedStatus{
    Created, Expired, Committed,
}
