package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.otc.ReservedStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Table("reserved_transfer")
data class ReservedTransferModel(
        @Id @Column("id") var id: Long?,
        @Column("reserve_number") var reserveNumber: String,
        @Column("source_symbol") var sourceSymbol: Long,
        @Column("dest_symbol") var destSymbol: Long,
        @Column("sender_wallet_type") var senderWalletType: String,
        @Column("sender_uuid") var senderUuid: String,
        @Column("receiver_wallet_type") var receiverWalletType: String,
        @Column("receiver_uuid") var receiverUuid: String,
        @Column("source_amount") var sourceAmount: BigDecimal,
        @Column("reserved_dest_amount") var reservedDestAmount: BigDecimal,
        @Column("reserve_date") var reserveDate: LocalDateTime? = LocalDateTime.now(),
        @Column("exp_date") var expDate: LocalDateTime? = null,
        var status: ReservedStatus?=null
)
