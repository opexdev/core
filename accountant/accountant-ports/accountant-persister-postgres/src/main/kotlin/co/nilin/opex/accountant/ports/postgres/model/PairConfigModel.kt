package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("pair_config")
data class PairConfigModel(
    @Id val pair: String,
    @Column("left_side_wallet_symbol") val leftSideWalletSymbol: String, //can be same as pair left side
    @Column("right_side_wallet_symbol") val rightSideWalletSymbol: String, //can be same as pair right side
    @Column("left_side_fraction") val leftSideFraction: Double,
    @Column("right_side_fraction") val rightSideFraction: Double
)