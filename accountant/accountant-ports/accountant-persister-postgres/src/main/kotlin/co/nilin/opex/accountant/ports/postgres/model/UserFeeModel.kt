package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("user_fee")
data class UserFeeModel(
    val uuid: String,
    val quoteSymbol: String,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal,
    @Id val id: Long? = null
)