package co.nilin.mixchange.port.wallet.postgres.model

import co.nilin.mixchange.wallet.core.model.WalletOwner
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("wallet_owner")
class WalletOwnerModel(
    @Id @Column("id") var id_: Long?,
    @Column("uuid") var uuid_: String,
    @Column("title") var title_: String,
    @Column("level") var level_: String
): WalletOwner {
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
}