package co.nilin.opex.wallet.ports.postgres.model

import co.nilin.opex.wallet.core.model.WalletLimitAction
import co.nilin.opex.wallet.core.model.WalletType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("wallet_limits")
data class WalletLimitsModel(
    @Id var id: Long?,
    val level: String?,
    val owner: Long?,
    val action: WalletLimitAction?, //withdraw or deposit
    val currency: String?,
    val walletType: WalletType,
    val walletId: Long?,
    val dailyTotal: BigDecimal?,
    val dailyCount: Int?,
    val monthlyTotal: BigDecimal?,
    val monthlyCount: Int?
)