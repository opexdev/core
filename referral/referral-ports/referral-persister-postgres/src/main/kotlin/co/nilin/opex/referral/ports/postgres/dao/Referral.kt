package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("referrals")
data class Referral(
    @Id var id: Long?,
    var code: String,
    var parent: Long,
    var referrerCommission: BigDecimal,
    var referentCommission: BigDecimal
)
