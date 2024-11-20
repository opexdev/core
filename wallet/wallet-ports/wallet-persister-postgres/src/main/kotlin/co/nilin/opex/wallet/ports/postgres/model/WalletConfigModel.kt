package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("wallet_config")
data class WalletConfigModel(@Id val name: String, val mainCurrency: String)