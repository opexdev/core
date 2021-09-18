package co.nilin.opex.port.wallet.postgres.model

import co.nilin.opex.wallet.core.model.WalletOwner
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("wallet_owner")
class WalletOwnerModel(
    @Id @Column("id") var id_: Long?,
    @Column("uuid") var uuid_: String,
    @Column("title") var title_: String,
    @Column("level") var level_: String,
    @Column("trade_allowed") var isTradeAllowed_: Boolean = true,
    @Column("withdraw_allowed") var isWithdrawAllowed_: Boolean = true,
    @Column("deposit_allowed") var isDepositAllowed_: Boolean = true,
) : WalletOwner {

    override fun id(): Long? {
        return id_
    }

    override fun uuid(): String {
        return uuid_
    }

    override fun title(): String {
        return title_
    }

    override fun level(): String {
        return level_
    }

    override fun isTradeAllowed(): Boolean {
        return isTradeAllowed_
    }

    override fun isWithdrawAllowed(): Boolean {
        return isWithdrawAllowed_
    }

    override fun isDepositAllowed(): Boolean {
        return isDepositAllowed_
    }
}