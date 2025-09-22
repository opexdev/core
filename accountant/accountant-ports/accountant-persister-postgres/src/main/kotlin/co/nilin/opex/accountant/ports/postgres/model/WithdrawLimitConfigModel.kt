package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("withdraw_limit_config")
data class WithdrawLimitConfigModel(
    @Id val id: Long? = null,
    val userLevel: String,
    val dailyMaxAmount: BigDecimal,

    )
