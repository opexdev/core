package co.nilin.opex.referral.ports.postgres.dao

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("referral_codes")
data class ReferralCode(
    @Id var id: Long?,
    var uuid: String,
    var code: String,
    var referentCommission: BigDecimal
)
