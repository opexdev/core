package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("wallet_owner")
data class WalletOwnerModel(
    @Id var id: Long?,
    val uuid: String,
    var title: String,
    var level: String,
    @Column("trade_allowed")
    var isTradeAllowed: Boolean = true,
    @Column("withdraw_allowed")
    var isWithdrawAllowed: Boolean = true,
    @Column("deposit_allowed")
    var isDepositAllowed: Boolean = true,
)