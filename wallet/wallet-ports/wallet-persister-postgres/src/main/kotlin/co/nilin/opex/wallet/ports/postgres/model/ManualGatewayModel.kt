package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_manual_gateway")

data class ManualGatewayModel(@Id var id: Long?,
                              @Column("gateway_uuid") val gatewayUuid: String,
                              @Column("currency_symbol") val currencySymbol: String,
                              @Column("allowed_for") var allowedFor: String,
                              @Column("withdraw_allowed") var withdrawAllowed: Boolean?=true,
                              @Column("deposit_allowed") var depositAllowed: Boolean?=true,
                              @Column("withdraw_fee") var withdrawFee: BigDecimal?= BigDecimal.ZERO,
                              @Column("withdraw_min") var withdrawMin: BigDecimal?= BigDecimal.ZERO,
                              @Column("withdraw_max") var withdrawMax: BigDecimal?= BigDecimal.ZERO,
                              @Column("deposit_min") var depositMin: BigDecimal?= BigDecimal.ZERO,
                              @Column("deposit_max") var depositMax: BigDecimal?= BigDecimal.ZERO,
                              @Column("is_active") var isActive: Boolean?=true,)
