package co.nilin.opex.wallet.ports.postgres.dto

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "wallet_stat_exclusion")
data class WalletStatExclusion(
    val walletId: Long,
    @Id
    val id: Long? = null
)