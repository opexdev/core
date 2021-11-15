package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("wallet_config")
class WalletConfigModel(@Id val name: String, @Column("main_currency") val mainCurrency: String)