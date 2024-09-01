package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.WalletType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("wallet")
data class WalletModel(
    val owner: Long,
    @Column("wallet_type")
    val type: WalletType,
    val currency: String,
    val balance: BigDecimal,
    @Id
    val id: Long? = null,
    @Version
    var version: Long? = null
)