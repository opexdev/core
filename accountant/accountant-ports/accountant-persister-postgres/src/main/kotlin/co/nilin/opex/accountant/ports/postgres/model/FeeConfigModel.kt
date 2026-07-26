package co.nilin.opex.accountant.ports.postgres.model

import co.nilin.opex.accountant.core.model.Condition
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table(name = "fee_config")
data class FeeConfigModel(
    @Id
    val name: String,
    val displayOrder: Int,
    val minAssetVolume: BigDecimal,
    val maxAssetVolume: BigDecimal? = null,
    val minTradeVolume: BigDecimal,
    val maxTradeVolume: BigDecimal? = null,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal,
    val condition: Condition,
)

