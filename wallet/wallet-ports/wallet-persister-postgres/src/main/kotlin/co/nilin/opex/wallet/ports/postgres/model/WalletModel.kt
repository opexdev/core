package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("wallet")
data class WalletModel(
    @Id val id: Long?,
    val owner: Long,
    @Column("wallet_type")
    val type: String,
    val currency: String,
    val balance: BigDecimal,
    @Version
    var version: Long? = null
)