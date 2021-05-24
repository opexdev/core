package co.nilin.mixchange.port.wallet.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("user_limits")
class UserLimitsModel(
    @Id val id: Long?,
    val level: String?,
    val owner: Long?,
    val action: String, //withdraw or deposit
    val walletType: String,
    @Column("daily_total") val dailyTotal: BigDecimal?,
    @Column("daily_count") val dailyCount: Int?,
    @Column("monthly_total") val monthlyTotal: BigDecimal?,
    @Column("monthly_count") val monthlyCount: Int?
)