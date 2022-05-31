package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("pair_fee_config")
class PairFeeConfigModel(
    val id: Long?,
    @Column("pair_config_id") val pairConfigId: String,
    @Column("direction") val direction: String,
    @Column("user_level") val userLevel: String,
    @Column("maker_fee") val makerFee: BigDecimal,
    @Column("taker_fee") val takerFee: BigDecimal
)