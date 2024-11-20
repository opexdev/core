package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.WalletType
import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Table("reserved_transfer")
data class ReservedTransferModel(
    @Id var id: Long?,
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
    var rate: BigDecimal? = null
)
