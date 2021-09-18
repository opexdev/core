package co.nilin.opex.port.wallet.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("wallet")
data class WalletModel(
    @Id @Column("id") val id: Long?,
    @Column("owner") val owner: Long,
    @Column("wallet_type") val type: String,
    @Column("currency") val currency: String,
    @Column("balance") val balance: BigDecimal
)