package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("wallet_owner")
data class WalletOwnerModel(
    @Id @Column("id") var id: Long?,
    @Column("uuid") var uuid: String,
    @Column("title") var title: String,
    @Column("level") var level: String,
    @Column("trade_allowed") var isTradeAllowed: Boolean = true,
    @Column("withdraw_allowed") var isWithdrawAllowed: Boolean = true,
    @Column("deposit_allowed") var isDepositAllowed: Boolean = true,
)